import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// PLUS BESOIN de RouterLink ici
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { AudioService } from '../../services/audio';
import { Sound, SoundFilter, Module } from '../../models/interfaces';
// IMPORT DU COMPOSANT ENFANT
import { SoundAddComponent } from './sound-add/sound-add';
import { I18nService } from '../../services/i18n';

@Component({
  selector: 'app-sounds',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatTooltipModule,
    SoundAddComponent // Ajouté aux imports
  ],
  templateUrl: './sounds.html', // On garde le même fichier HTML
  styleUrl: './sounds.css',
})
export class Sounds implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  private audioService = inject(AudioService);
  readonly i18n = inject(I18nService);

  readonly isAdmin = this.authService.isAdmin;
  readonly canEdit = this.authService.canEdit;

  // --- NOUVEAU : État de l'affichage ---
  // 'list' = on voit les sons, 'add' = on voit le formulaire
  viewMode = signal<'list' | 'add'>('list');

  selectedRole = '';
  profiles: { role: string; name: string }[] = [];

  sounds = signal<Sound[]>([]);
  soundFilter = signal<SoundFilter>('all');

  // ... (Gardez editingSoundId, editSoundData, etc. comme avant) ...
  editingSoundId = signal<number | null>(null);
  editSoundData = signal<{ name: string, type: string }>({ name: '', type: '' });
  editSoundError = signal('');
  currentlyPlayingId = this.audioService.currentlyPlayingId;

  // Sound-module assignment
  soundModules = signal<{ id: number, name: string }[]>([]);
  soundModuleCounts = signal<Record<number, number>>({});
  allModules = signal<{ id: number, name: string }[]>([]);
  assignToModuleId = signal<number | null>(null);

  filteredSounds = computed(() => {
    const filter = this.soundFilter();
    const allSounds = this.sounds();
    if (filter === 'all') return allSounds;
    return allSounds.filter(s => s.type === filter);
  });

  ngOnInit() {
    if (this.isAdmin()) {
      this.api.getRoles().subscribe(roles => {
        this.profiles = [{ role: '', name: this.i18n.t('common.allProfiles') }, ...roles.map(r => ({ role: r, name: r.charAt(0).toUpperCase() + r.slice(1) }))];
      });
    }
    this.loadData();
  }

  loadData() {
    const filterRole = this.isAdmin() ? (this.selectedRole || undefined) : undefined;
    this.api.getSounds(filterRole).subscribe(data => {
      this.sounds.set(data);
      // Load module count for each sound
      data.forEach(s => {
        this.api.getSoundModules(s.id).subscribe(modules => {
          this.soundModuleCounts.update(counts => ({ ...counts, [s.id]: modules.length }));
        });
      });
    });
    // Load all modules for the assign dropdown
    this.api.getModules(filterRole).subscribe(modules => {
      this.allModules.set(modules.map(m => ({ id: m.id, name: m.name })));
    });
  }

  // --- ACTIONS DE NAVIGATION ---
  openAddPage() {
    this.viewMode.set('add'); // Passe en mode ajout
  }

  closeAddPage(shouldReload: boolean) {
    this.viewMode.set('list'); // Revient en mode liste
    if (shouldReload) {
      this.loadData(); // Recharge les données si un son a été ajouté
    }
  }

  // ... (Gardez onProfileChange, setSoundFilter, playSound, deleteSound, etc.) ...
  onProfileChange(role: string) { this.selectedRole = role; this.loadData(); }
  setSoundFilter(filter: SoundFilter) { this.soundFilter.set(filter); }
  playSound(sound: Sound) { this.audioService.playSound(this.api.getSoundFileUrl(sound.id), sound.id); }

  // ... (Gardez tout le bloc Edit et Delete du code précédent) ...
  startEditSound(sound: Sound) {
    this.editingSoundId.set(sound.id);
    this.editSoundData.set({ name: sound.name, type: sound.type });
    this.assignToModuleId.set(null);
    // Load modules this sound is assigned to
    this.api.getSoundModules(sound.id).subscribe(modules => this.soundModules.set(modules));
  }
  cancelEditSound() { this.editingSoundId.set(null); this.soundModules.set([]); }
  updateEditSoundName(name: string) { this.editSoundData.update(d => ({ ...d, name })); }
  updateEditSoundType(type: string) { this.editSoundData.update(d => ({ ...d, type })); }
  saveEditSound(id: number) { /* Code update... */
    const data = this.editSoundData();
    this.api.updateSound(id, data).subscribe(updated => {
      this.sounds.update(s => s.map(x => x.id === id ? updated : x));
      this.cancelEditSound();
    });
  }
  deleteSound(sound: Sound) { /* Code delete... */
    if (confirm(this.i18n.t('sounds.deleteConfirm'))) this.api.deleteSound(sound.id).subscribe(() => this.sounds.update(s => s.filter(x => x.id !== sound.id)));
  }

  /** Translate database sound type to current language */
  translateType(type: string): string {
    const typeMap: Record<string, string> = {
      'Vocal': this.i18n.t('sounds.filterVocal'),
      'Ambiance': this.i18n.t('sounds.filterAmbiance'),
      'Naturel': this.i18n.t('sounds.filterNatural'),
      'Autre': this.i18n.t('sounds.filterOther')
    };
    return typeMap[type] || type;
  }

  unassignFromModule(soundId: number, moduleId: number) {
    this.api.unassignSoundFromModule(moduleId, soundId).subscribe(() => {
      this.soundModules.update(list => list.filter(m => m.id !== moduleId));
      this.soundModuleCounts.update(counts => ({ ...counts, [soundId]: (counts[soundId] || 1) - 1 }));
    });
  }

  assignToModule(soundId: number) {
    const moduleId = this.assignToModuleId();
    if (!moduleId) return;
    this.api.assignSoundToModule(moduleId, soundId).subscribe(() => {
      this.assignToModuleId.set(null);
      this.api.getSoundModules(soundId).subscribe(modules => this.soundModules.set(modules));
      this.soundModuleCounts.update(counts => ({ ...counts, [soundId]: (counts[soundId] || 0) + 1 }));
    });
  }

  getAvailableModulesForSound(): { id: number, name: string }[] {
    const assigned = this.soundModules();
    return this.allModules().filter(m => !assigned.some(a => a.id === m.id));
  }
}