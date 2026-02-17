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
import { Sound, SoundFilter } from '../../models/interfaces';
// IMPORT DU COMPOSANT ENFANT
import { SoundAddComponent } from './sound-add/sound-add';

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

  readonly isAdmin = this.authService.isAdmin;
  readonly canEdit = this.authService.canEdit;

  // --- NOUVEAU : État de l'affichage ---
  // 'list' = on voit les sons, 'add' = on voit le formulaire
  viewMode = signal<'list' | 'add'>('list');

  selectedRole = '';
  profiles: { role: string; name: string }[] = [{ role: '', name: 'Tous les profils' }];
  
  sounds = signal<Sound[]>([]);
  soundFilter = signal<SoundFilter>('all');
  
  // ... (Gardez editingSoundId, editSoundData, etc. comme avant) ...
  editingSoundId = signal<number | null>(null);
  editSoundData = signal<{ name: string, type: string }>({ name: '', type: '' });
  editSoundError = signal('');
  currentlyPlayingId = this.audioService.currentlyPlayingId;

  filteredSounds = computed(() => {
    const filter = this.soundFilter();
    const allSounds = this.sounds();
    if (filter === 'all') return allSounds;
    return allSounds.filter(s => s.type === filter);
  });

  ngOnInit() {
    if (this.isAdmin()) {
      this.api.getRoles().subscribe(roles => {
        this.profiles = [{ role: '', name: 'Tous les profils' }, ...roles.map(r => ({ role: r, name: r.charAt(0).toUpperCase() + r.slice(1) }))];
      });
    }
    this.loadData();
  }

  loadData() {
    const filterRole = this.isAdmin() ? (this.selectedRole || undefined) : undefined;
    this.api.getSounds(filterRole).subscribe(data => this.sounds.set(data));
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
  startEditSound(sound: Sound) { this.editingSoundId.set(sound.id); this.editSoundData.set({ name: sound.name, type: sound.type }); }
  cancelEditSound() { this.editingSoundId.set(null); }
  updateEditSoundName(name: string) { this.editSoundData.update(d => ({...d, name})); }
  updateEditSoundType(type: string) { this.editSoundData.update(d => ({...d, type})); }
  saveEditSound(id: number) { /* Code update... */ 
      const data = this.editSoundData();
      this.api.updateSound(id, data).subscribe(updated => {
          this.sounds.update(s => s.map(x => x.id === id ? updated : x));
          this.cancelEditSound();
      });
  }
  deleteSound(sound: Sound) { /* Code delete... */ 
      if(confirm('Supprimer ?')) this.api.deleteSound(sound.id).subscribe(() => this.sounds.update(s => s.filter(x => x.id !== sound.id)));
  }
}