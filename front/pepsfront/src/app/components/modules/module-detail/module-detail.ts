/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description Module detail component. Allows viewing, editing, and deleting a module.
 */
import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
import { Module, Sound } from '../../../models/interfaces';
import { I18nService } from '../../../services/i18n';

@Component({
  selector: 'app-module-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatSliderModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './module-detail.html',
  styleUrl: './module-detail.css',
})
export class ModuleDetail implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  readonly i18n = inject(I18nService);

  readonly canEdit = this.authService.canEdit;

  module = signal<Module | null>(null);
  isLoading = signal(true);
  loadError = signal('');

  // Sound assignment
  assignedSounds = signal<Sound[]>([]);
  allProfileSounds = signal<Sound[]>([]);
  selectedSoundId = signal<number | null>(null);

  availableSounds = computed(() => {
    const assigned = this.assignedSounds();
    const all = this.allProfileSounds();
    return all.filter(s => !assigned.some(a => a.id === s.id));
  });

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id || isNaN(id)) {
      this.loadError.set(this.i18n.t('modules.invalidId') || 'Identifiant de module invalide.');
      this.isLoading.set(false);
      return;
    }

    this.api.getModuleById(id).subscribe({
      next: (mod) => {
        this.module.set(mod);
        this.loadSoundAssignments(mod.id);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading module:', err);
        this.loadError.set(this.i18n.t('modules.loadError') || 'Module introuvable ou erreur de chargement.');
        this.isLoading.set(false);
      },
    });
  }

  loadSoundAssignments(moduleId: number) {
    this.api.getModuleSounds(moduleId).subscribe(sounds => this.assignedSounds.set(sounds));
    // Load all sounds for the same profile to populate the dropdown
    this.api.getSounds().subscribe(sounds => this.allProfileSounds.set(sounds));
  }

  assignSound() {
    const soundId = this.selectedSoundId();
    const mod = this.module();
    if (!soundId || !mod) return;
    this.api.assignSoundToModule(mod.id, soundId).subscribe(() => {
      this.selectedSoundId.set(null);
      this.loadSoundAssignments(mod.id);
    });
  }

  unassignSound(soundId: number) {
    const mod = this.module();
    if (!mod) return;
    this.api.unassignSoundFromModule(mod.id, soundId).subscribe(() => {
      this.loadSoundAssignments(mod.id);
    });
  }

  formatVolumeLabel(value: number): string {
    return `${value}%`;
  }

  onSave() {
    const moduleToSave = this.module();
    if (!moduleToSave) return;

    if (!moduleToSave.name || moduleToSave.name.trim() === '') {
      alert(this.i18n.t('modules.nameRequiredFull'));
      return;
    }

    if (!moduleToSave.ip || moduleToSave.ip.trim() === '') {
      alert(this.i18n.t('modules.ipRequiredFull'));
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(moduleToSave.ip)) {
      alert(this.i18n.t('modules.ipInvalid'));
      return;
    }

    if (moduleToSave.config.volume < 0 || moduleToSave.config.volume > 100) {
      alert(this.i18n.t('modules.volumeRange'));
      return;
    }

    this.api.updateModule(moduleToSave.id, moduleToSave).subscribe({
      next: () => this.router.navigate(['/modules']),
      error: (err) => {
        console.error('Error saving module:', err);
        alert(err.error?.error || this.i18n.t('modules.saveError'));
      },
    });
  }

  onCancel() {
    this.router.navigate(['/modules']);
  }

  onDelete() {
    const moduleToDelete = this.module();
    if (!moduleToDelete) return;

    if (!confirm(`${this.i18n.t('modules.deleteConfirm')} "${moduleToDelete.name}" ?`)) {
      return;
    }

    this.api.deleteModule(moduleToDelete.id).subscribe({
      next: () => this.router.navigate(['/modules']),
      error: (err) => {
        console.error('Error deleting module:', err);
        alert(err.error?.error || this.i18n.t('modules.deleteError'));
      },
    });
  }
}
