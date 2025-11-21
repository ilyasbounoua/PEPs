import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AudioService {
  private currentAudio: HTMLAudioElement | null = null;
  private currentlyPlaying = signal<number | null>(null);

  currentlyPlayingId = this.currentlyPlaying.asReadonly();

  playSound(url: string, id: number): void {
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio = null;
    }

    if (this.currentlyPlaying() === id) {
      this.currentlyPlaying.set(null);
      return;
    }

    // Use Audio() constructor or document.createElement('audio')
    this.currentAudio = new Audio(url);
    
    const playPromise = this.currentAudio.play();
    
    if (playPromise !== undefined) {
      playPromise
        .then(() => {
          this.currentlyPlaying.set(id);
        })
        .catch((error) => {
          console.error('Error playing audio:', error);
          alert('Erreur lors de la lecture du son: ' + error.message);
          this.currentlyPlaying.set(null);
          this.currentAudio = null;
        });
    }

    this.currentAudio.addEventListener('ended', () => {
      this.currentlyPlaying.set(null);
      this.currentAudio = null;
    });

    this.currentAudio.addEventListener('error', (e) => {
      console.error('Audio error event:', e);
      alert('Erreur lors de la lecture du son');
      this.currentlyPlaying.set(null);
      this.currentAudio = null;
    });
  }

  stopSound(): void {
    if (this.currentAudio) {
      this.currentAudio.pause();
      this.currentAudio = null;
    }
    this.currentlyPlaying.set(null);
  }
}
