import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../services/api';
import { AudioService } from '../../services/audio';
import { Sound, SoundFilter } from '../../models/interfaces';

@Component({
  selector: 'app-sounds',
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule
  ],
  templateUrl: './sounds.html',
  styleUrl: './sounds.css',
})
export class Sounds implements OnInit {
  private api = inject(ApiService);
  private audioService = inject(AudioService);

  sounds = signal<Sound[]>([]);
  soundFilter = signal<SoundFilter>('all');
  showAddSoundForm = signal(false);
  isUploading = signal(false);
  uploadError = signal('');
  
  newSound = signal({ name: '', type: '', file: null as File | null });
  
  editingSoundId = signal<number | null>(null);
  editSoundData = signal<{ name: string, type: string }>({ name: '', type: '' });
  editSoundError = signal('');

  currentlyPlayingId = this.audioService.currentlyPlayingId;

  filteredSounds = computed(() => {
    const filter = this.soundFilter();
    const allSounds = this.sounds();
    
    if (filter === 'all') {
      return allSounds;
    }
    
    return allSounds.filter(s => s.type === filter);
  });

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.api.getSounds().subscribe({
      next: (data) => this.sounds.set(data),
      error: (err) => console.error('Error loading sounds:', err)
    });
  }

  toggleAddForm() {
    this.showAddSoundForm.update(show => !show);
    this.newSound.set({ name: '', type: '', file: null });
    this.uploadError.set('');
  }

  setSoundFilter(filter: SoundFilter) {
    this.soundFilter.set(filter);
  }

  updateSoundName(name: string) {
    this.newSound.update(s => ({ ...s, name }));
  }

  updateSoundType(type: string) {
    this.newSound.update(s => ({ ...s, type }));
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const extension = file.name.split('.').pop()?.toLowerCase();
      
      if (!['mp3', 'wav', 'ogg', 'm4a'].includes(extension || '')) {
        this.uploadError.set('Format de fichier non supporté. Utilisez mp3, wav, ogg ou m4a');
        input.value = '';
        return;
      }

      this.newSound.update(current => ({ ...current, file }));
      this.uploadError.set('');
    }
  }

  uploadSound() {
    const sound = this.newSound();
    
    if (!sound.name || sound.name.trim() === '') {
      this.uploadError.set('Le nom est obligatoire');
      return;
    }

    if (!sound.type || sound.type.trim() === '') {
      this.uploadError.set('Le type est obligatoire');
      return;
    }

    if (!sound.file) {
      this.uploadError.set('Veuillez sélectionner un fichier');
      return;
    }

    const formData = new FormData();
    formData.append('name', sound.name);
    formData.append('type', sound.type);
    formData.append('file', sound.file, sound.file.name);

    this.isUploading.set(true);
    this.uploadError.set('');

    this.api.uploadSound(formData).subscribe({
      next: (newSound) => {
        this.sounds.update(sounds => [...sounds, newSound]);
        this.showAddSoundForm.set(false);
        this.newSound.set({ name: '', type: '', file: null });
        this.isUploading.set(false);
      },
      error: (err) => {
        console.error('Error uploading sound:', err);
        this.isUploading.set(false);
        if (err.error && err.error.error) {
          this.uploadError.set(err.error.error);
        } else {
          this.uploadError.set('Erreur lors de l\'upload du son');
        }
      }
    });
  }

  playSound(sound: Sound) {
    const url = this.api.getSoundFileUrl(sound.id);
    console.log('Playing sound:', sound.name, 'URL:', url);
    this.audioService.playSound(url, sound.id);
  }

  startEditSound(sound: Sound) {
    this.editingSoundId.set(sound.id);
    this.editSoundData.set({ name: sound.name, type: sound.type });
    this.editSoundError.set('');
  }

  cancelEditSound() {
    this.editingSoundId.set(null);
    this.editSoundData.set({ name: '', type: '' });
    this.editSoundError.set('');
  }

  updateEditSoundName(name: string) {
    this.editSoundData.update(data => ({ ...data, name }));
  }

  updateEditSoundType(type: string) {
    this.editSoundData.update(data => ({ ...data, type }));
  }

  saveEditSound(soundId: number) {
    const data = this.editSoundData();
    
    if (!data.name || data.name.trim() === '') {
      this.editSoundError.set('Le nom est obligatoire');
      return;
    }

    if (!data.type || data.type.trim() === '') {
      this.editSoundError.set('Le type est obligatoire');
      return;
    }

    this.api.updateSound(soundId, data).subscribe({
      next: (updatedSound) => {
        this.sounds.update(sounds => 
          sounds.map(s => s.id === updatedSound.id ? updatedSound : s)
        );
        this.cancelEditSound();
      },
      error: (err) => {
        console.error('Error updating sound:', err);
        if (err.error && err.error.error) {
          this.editSoundError.set(err.error.error);
        } else {
          this.editSoundError.set('Erreur lors de la modification du son');
        }
      }
    });
  }

  deleteSound(sound: Sound) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le son "${sound.name}" ?`)) {
      return;
    }

    this.api.deleteSound(sound.id).subscribe({
      next: () => {
        this.sounds.update(sounds => sounds.filter(s => s.id !== sound.id));
        if (this.currentlyPlayingId() === sound.id) {
          this.audioService.stopSound();
        }
      },
      error: (err) => {
        console.error('Error deleting sound:', err);
        if (err.error && err.error.error) {
          alert('Erreur: ' + err.error.error);
        } else {
          alert('Erreur lors de la suppression du son');
        }
      }
    });
  }
}
