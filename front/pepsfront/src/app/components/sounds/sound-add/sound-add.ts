import { Component, OnInit, inject, signal, output } from '@angular/core'; // Notez 'output'
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
import { I18nService } from '../../../services/i18n';

@Component({
  selector: 'app-sound-add', // Ce sélecteur est important
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule
  ],
  templateUrl: './sound-add.html',
  styleUrls: ['./sound-add.css']
})
export class SoundAddComponent implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  readonly i18n = inject(I18nService);

  // --- NOUVEAU : Événements vers le parent ---
  // Dit au parent de fermer le formulaire
  cancel = output<void>();
  // Dit au parent que c'est fini et qu'il faut recharger la liste
  soundAdded = output<void>();

  readonly isAdmin = this.authService.isAdmin;

  newSound = signal({ name: '', type: '', file: null as File | null });
  isUploading = signal(false);
  uploadError = signal('');

  selectedRole = '';
  profiles: { role: string; name: string }[] = [];
  availableModules = signal<{ id: number, name: string }[]>([]);
  selectedModuleId = signal<number | null>(null);

  ngOnInit() {
    if (this.isAdmin()) this.loadRoles();
    this.loadModules();
  }

  loadRoles() {
    this.api.getRoles().subscribe({
      next: (roles) => {
        this.profiles = roles.map(r => ({ role: r, name: r.charAt(0).toUpperCase() + r.slice(1) }));
      },
      error: (err) => console.error('Error loading roles:', err)
    });
  }

  loadModules() {
    const role = this.isAdmin() ? (this.selectedRole || undefined) : undefined;
    this.api.getModules(role).subscribe({
      next: (modules) => this.availableModules.set(modules.map(m => ({ id: m.id, name: m.name }))),
      error: (err) => console.error('Error loading modules:', err)
    });
  }

  updateSoundName(name: string) { this.newSound.update(s => ({ ...s, name })); }
  updateSoundType(type: string) { this.newSound.update(s => ({ ...s, type })); }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      const file = input.files[0];
      // Validate extension
      if (!['mp3', 'wav', 'ogg', 'm4a'].includes(file.name.split('.').pop()?.toLowerCase() || '')) {
        this.uploadError.set(this.i18n.t('sounds.formatError'));
        return;
      }
      // Validate file size
      if (file.size === 0) {
        this.uploadError.set(this.i18n.t('sounds.fileEmpty'));
        return;
      }
      const MAX_SIZE = 5 * 1024 * 1024;
      if (file.size > MAX_SIZE) {
        this.uploadError.set(this.i18n.t('sounds.fileTooLarge'));
        return;
      }
      this.newSound.update(c => ({ ...c, file }));
      this.uploadError.set('');
    }
  }

  uploadSound() {
    const sound = this.newSound();
    if (!sound.name?.trim()) { this.uploadError.set(this.i18n.t('sounds.nameRequired')); return; }
    if (!sound.type?.trim()) { this.uploadError.set(this.i18n.t('sounds.typeRequired')); return; }
    if (!sound.file) { this.uploadError.set(this.i18n.t('sounds.fileRequired')); return; }
    if (this.isAdmin() && !this.selectedRole) { this.uploadError.set(this.i18n.t('sounds.profileRequired')); return; }

    const formData = new FormData();
    formData.append('name', sound.name);
    formData.append('type', sound.type);
    formData.append('file', sound.file, sound.file.name);

    this.isUploading.set(true);

    const overrideRole = this.isAdmin() ? this.selectedRole : undefined;

    this.api.uploadSound(formData, overrideRole).subscribe({
      next: (created: any) => {
        const moduleId = this.selectedModuleId();
        if (moduleId && created?.id) {
          // Chain: assign to module after upload
          this.api.assignSoundToModule(moduleId, created.id).subscribe({
            next: () => { this.isUploading.set(false); this.soundAdded.emit(); },
            error: () => { this.isUploading.set(false); this.soundAdded.emit(); } // sound was created, emit anyway
          });
        } else {
          this.isUploading.set(false);
          this.soundAdded.emit();
        }
      },
      error: (err) => {
        this.isUploading.set(false);
        this.uploadError.set(err.error?.error || this.i18n.t('sounds.uploadError'));
      }
    });
  }

  onCancel() {
    this.cancel.emit(); // On clique sur Annuler
  }
}