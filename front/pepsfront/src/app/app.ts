import { Component, ChangeDetectionStrategy, computed, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http';

import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatRadioModule } from '@angular/material/radio';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';

// --- Interfaces (issues du CDC) ---
interface StatCard {
  totalInteractions: number;
  activeModules: number;
  lastInteraction: string;
}

interface Interaction {
  id: number;
  date: string;
  module: string;
  type: string;
}

interface Module {
  id: number;
  name: string;
  location: string;
  status: 'Actif' | 'Inactif';
  ip: string;
  config: {
    volume: number;
    mode: 'Manuel' | 'Automatique';
    actif: boolean;
    son: boolean;
  };
}

interface DailyData {
  time: string;
  count: number;
}

interface Sound {
  id: number;
  name: string;
  type: string;
  extension: string;
  fileName: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    MatSidenavModule,
    MatListModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatInputModule,
    MatFormFieldModule,
    MatRadioModule,
    MatSlideToggleModule,
    MatSliderModule,
    MatSelectModule,
    MatTooltipModule
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],  
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit {
  http = inject(HttpClient);
  private readonly BASE_URL = 'http://localhost:8080/PEPs_back';

  isLoggedIn = signal(false);
  loginError = signal('');
  readonly correctHash = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918';
  
  currentPage = signal('dashboard');
  isSidenavOpen = signal(true);

  stats = signal<StatCard>({
    totalInteractions: 0,
    activeModules: 0,
    lastInteraction: 'Chargement...'
  });
  dailyChartData = signal<DailyData[]>([]);

  filter = signal('all');
  interactions = signal<Interaction[]>([]);
  displayedColumns: string[] = ['date', 'module', 'type'];

  filteredInteractions = computed(() => {
    const f = this.filter();
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const weekAgo = new Date(today);
    weekAgo.setDate(weekAgo.getDate() - 7);

    const allInteractions = this.interactions();

    if (f === 'today') {
      return allInteractions.filter(i => {
        const interactionDate = new Date(i.date);
        return interactionDate >= today;
      });
    }
    if (f === 'yesterday') {
      return allInteractions.filter(i => {
        const interactionDate = new Date(i.date);
        return interactionDate >= yesterday && interactionDate < today;
      });
    }
    if (f === 'week') {
      return allInteractions.filter(i => {
        const interactionDate = new Date(i.date);
        return interactionDate >= weekAgo;
      });
    }
    return allInteractions;
  });

  modules = signal<Module[]>([]);
  selectedModule = signal<Module | undefined>(undefined);
  newModule = signal<Omit<Module, 'id'>>({
    name: '',
    location: '',
    status: 'Inactif',
    ip: '',
    config: {
      volume: 50,
      mode: 'Manuel',
      actif: false,
      son: true
    }
  });
  addModuleError = signal('');
  
  sounds = signal<Sound[]>([]);
  isUploadingSoundSignal = signal(false);
  uploadSoundError = signal('');
  newSound = signal({ name: '', type: '', file: null as File | null });
  showAddSoundForm = signal(false);
  currentlyPlayingSound = signal<number | null>(null);
  audioElement: HTMLAudioElement | null = null;
  soundFilter = signal('all');
  editingSoundId = signal<number | null>(null);
  editSoundData = signal<{ name: string, type: string }>({ name: '', type: '' });
  editSoundError = signal('');

  pageTitle = computed(() => {
    switch(this.currentPage()) {
      case 'dashboard': return 'Tableau de Bord';
      case 'interactions': return 'Historique des Interactions';
      case 'modules': return 'Gestion des Modules';
      case 'module-detail': return `Détail: ${this.selectedModule()?.name || ''}`;
      case 'add-module': return 'Ajouter un Module';
      case 'sounds': return 'Bibliothèque de Sons';
      default: return 'PEP\'S';
    }
  });

  filteredSounds = computed(() => {
    const filter = this.soundFilter();
    const allSounds = this.sounds();
    
    if (filter === 'all') {
      return allSounds;
    }
    
    return allSounds.filter(s => s.type === filter);
  });

  ngOnInit() {
    if (this.isLoggedIn()) {
      this.loadAllData();
    }
  }

  loadAllData() {
    this.loadDashboardData();
    this.loadDailyStats();
    this.loadInteractions();
    this.loadModules();
    this.loadSounds();
  }

  loadDashboardData() {
    this.stats.update(current => ({
      ...current,
      totalInteractions: 0,
      lastInteraction: 'Chargement...'
    }));

    this.http.get<StatCard>(`${this.BASE_URL}/dashboard`).subscribe({
      next: (data) => {
        this.stats.set({
          totalInteractions: data.totalInteractions,
          activeModules: data.activeModules,
          lastInteraction: data.lastInteraction
        });
      },
      error: (err) => {
        console.error('Erreur de chargement du dashboard:', err);
        this.stats.update(current => ({
          ...current,
          totalInteractions: 0,
          lastInteraction: 'Erreur de connexion'
        }));
      }
    });
  }

  loadDailyStats() {
    this.http.get<DailyData[]>(`${this.BASE_URL}/daily-stats`).subscribe({
      next: (data) => {
        this.dailyChartData.set(data);
      },
      error: (err) => {
        console.error('Erreur de chargement des stats journalières:', err);
      }
    });
  }

  loadInteractions() {
    this.http.get<any[]>(`${this.BASE_URL}/interactions`).subscribe({
      next: (data) => {
        const formattedInteractions = data.map(i => ({
          id: i.id,
          date: new Date(i.date).toISOString().replace('T', ' ').substring(0, 19),
          module: i.module,
          type: i.type
        }));
        this.interactions.set(formattedInteractions);
      },
      error: (err) => {
        console.error('Erreur de chargement des interactions:', err);
      }
    });
  }

  loadModules() {
    this.http.get<Module[]>(`${this.BASE_URL}/modules`).subscribe({
      next: (data) => {
        this.modules.set(data);
      },
      error: (err) => {
        console.error('Erreur de chargement des modules:', err);
      }
    });
  }

  loadSounds() {
    this.http.get<Sound[]>(`${this.BASE_URL}/sounds`).subscribe({
      next: (data) => {
        this.sounds.set(data);
      },
      error: (err) => {
        console.error('Erreur de chargement des sons:', err);
      }
    });
  }

  async checkPassword(event: Event) {
    event.preventDefault();
    this.loginError.set('');
    const form = event.target as HTMLFormElement;
    const passwordInput = form.elements.namedItem('password') as HTMLInputElement;
    const password = passwordInput.value;

    if (!password) {
      this.loginError.set('Veuillez entrer un mot de passe.');
      return;
    }

    try {
      const encoder = new TextEncoder();
      const data = encoder.encode(password);
      const hashBuffer = await crypto.subtle.digest('SHA-256', data);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      const hexHash = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

      if (hexHash === this.correctHash) {
        this.isLoggedIn.set(true);
        this.loadAllData();
      } else {
        this.loginError.set('Mot de passe incorrect.');
      }
    } catch (err) {
      console.error('Erreur de hachage:', err);
      this.loginError.set('Une erreur est survenue.');
    }
  }

  setCurrentPage(page: string) {
    this.currentPage.set(page);
    this.selectedModule.set(undefined); // Reset la sélection de module
  }

  toggleSidenav() {
    this.isSidenavOpen.update(open => !open);
  }

  refreshStats() {
    this.loadAllData();
  }

  setFilter(newFilter: string) {
    this.filter.set(newFilter);
  }

  selectModule(module: Module) {
    this.selectedModule.set(module);
    this.currentPage.set('module-detail');
  }

  saveModuleConfig(moduleToSave: Module) {
    if (!moduleToSave.name || moduleToSave.name.trim() === '') {
      alert('Le nom du module est obligatoire');
      return;
    }

    if (!moduleToSave.ip || moduleToSave.ip.trim() === '') {
      alert('L\'adresse IP est obligatoire');
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(moduleToSave.ip)) {
      alert('Format d\'adresse IP invalide');
      return;
    }

    if (moduleToSave.config.volume < 0 || moduleToSave.config.volume > 100) {
      alert('Le volume doit être entre 0 et 100');
      return;
    }

    this.http.put<Module>(`${this.BASE_URL}/modules/${moduleToSave.id}`, moduleToSave).subscribe({
      next: (updatedModule) => {
        this.modules.update(currentModules =>
          currentModules.map(m => m.id === updatedModule.id ? updatedModule : m)
        );
        this.currentPage.set('modules');
        this.selectedModule.set(undefined);
        this.loadDashboardData();
      },
      error: (err) => {
        console.error('Erreur de sauvegarde:', err);
        if (err.error && err.error.error) {
          alert('Erreur: ' + err.error.error);
        } else {
          alert('Erreur lors de la sauvegarde de la configuration');
        }
      }
    });
  }

  exportAsCsv() {
    const data = this.filteredInteractions();
    if (data.length === 0) {
      return;
    }

    const headers = ['ID', 'Date', 'Module', 'Type'];
    const rows = data.map(i => [i.id, `"${i.date}"`, i.module, i.type].join(','));
    
    const csvContent = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'interactions-aras.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  formatVolumeLabel(value: number): string {
    return `${value}%`;
  }

  playSound(sound: Sound) {
    if (this.audioElement) {
      this.audioElement.pause();
      this.audioElement = null;
    }

    if (this.currentlyPlayingSound() === sound.id) {
      this.currentlyPlayingSound.set(null);
      return;
    }

    this.audioElement = new Audio(`${this.BASE_URL}/sounds/${sound.id}/file`);
    this.audioElement.play();
    this.currentlyPlayingSound.set(sound.id);

    this.audioElement.addEventListener('ended', () => {
      this.currentlyPlayingSound.set(null);
    });

    this.audioElement.addEventListener('error', () => {
      alert('Erreur lors de la lecture du son');
      this.currentlyPlayingSound.set(null);
    });
  }

  stopSound() {
    if (this.audioElement) {
      this.audioElement.pause();
      this.audioElement = null;
    }
    this.currentlyPlayingSound.set(null);
  }

  toggleAddSoundForm() {
    this.showAddSoundForm.update(show => !show);
    this.newSound.set({ name: '', type: '', file: null });
    this.uploadSoundError.set('');
  }

  updateSoundName(name: string) {
    this.newSound.update(s => ({ ...s, name }));
  }

  updateSoundType(type: string) {
    this.newSound.update(s => ({ ...s, type }));
  }

  onSoundFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    console.log('File input changed:', input.files);
    
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      console.log('File selected:', file.name, file.size, file.type);
      
      const extension = file.name.split('.').pop()?.toLowerCase();
      console.log('Extension:', extension);
      
      if (!['mp3', 'wav', 'ogg', 'm4a'].includes(extension || '')) {
        this.uploadSoundError.set('Format de fichier non supporté. Utilisez mp3, wav, ogg ou m4a');
        input.value = '';
        return;
      }

      this.newSound.update(current => ({ ...current, file }));
      this.uploadSoundError.set('');
      console.log('File stored in newSound signal');
    } else {
      console.log('No file selected');
    }
  }

  uploadSound() {
    const sound = this.newSound();
    
    console.log('Upload sound called', sound);
    
    if (!sound.name || sound.name.trim() === '') {
      this.uploadSoundError.set('Le nom est obligatoire');
      return;
    }

    if (!sound.type || sound.type.trim() === '') {
      this.uploadSoundError.set('Le type est obligatoire');
      return;
    }

    if (!sound.file) {
      this.uploadSoundError.set('Veuillez sélectionner un fichier');
      return;
    }

    console.log('Creating FormData...');
    const formData = new FormData();
    formData.append('name', sound.name);
    formData.append('type', sound.type);
    formData.append('file', sound.file, sound.file.name);

    console.log('FormData entries:');
    formData.forEach((value, key) => {
      console.log(key, value);
    });

    this.isUploadingSoundSignal.set(true);
    this.uploadSoundError.set('');

    console.log('Sending request to:', `${this.BASE_URL}/sounds`);

    this.http.post<Sound>(`${this.BASE_URL}/sounds`, formData).subscribe({
      next: (newSound) => {
        console.log('Upload successful:', newSound);
        this.sounds.update(sounds => [...sounds, newSound]);
        this.showAddSoundForm.set(false);
        this.newSound.set({ name: '', type: '', file: null });
        this.isUploadingSoundSignal.set(false);
      },
      error: (err) => {
        console.error('Erreur upload détaillée:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        console.error('Error error:', err.error);
        this.isUploadingSoundSignal.set(false);
        if (err.error && err.error.error) {
          this.uploadSoundError.set(err.error.error);
        } else {
          this.uploadSoundError.set('Erreur lors de l\'upload du son: ' + (err.message || 'Erreur inconnue'));
        }
      }
    });
  }

  deleteSound(sound: Sound) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le son "${sound.name}" ?`)) {
      return;
    }

    this.http.delete(`${this.BASE_URL}/sounds/${sound.id}`).subscribe({
      next: () => {
        this.sounds.update(sounds => sounds.filter(s => s.id !== sound.id));
        if (this.currentlyPlayingSound() === sound.id) {
          this.stopSound();
        }
      },
      error: (err) => {
        console.error('Erreur suppression:', err);
        if (err.error && err.error.error) {
          alert('Erreur: ' + err.error.error);
        } else {
          alert('Erreur lors de la suppression du son');
        }
      }
    });
  }

  showAddModuleForm() {
    this.newModule.set({
      name: '',
      location: '',
      status: 'Inactif',
      ip: '',
      config: {
        volume: 50,
        mode: 'Manuel',
        actif: false,
        son: true
      }
    });
    this.addModuleError.set('');
    this.currentPage.set('add-module');
  }

  updateNewModuleName(name: string) {
    this.newModule.update(m => ({ ...m, name }));
  }

  updateNewModuleIp(ip: string) {
    this.newModule.update(m => ({ ...m, ip }));
  }

  updateNewModuleActif(actif: boolean) {
    this.newModule.update(m => ({ ...m, config: { ...m.config, actif } }));
  }

  updateNewModuleVolume(volume: number) {
    this.newModule.update(m => ({ ...m, config: { ...m.config, volume } }));
  }

  updateNewModuleMode(mode: 'Manuel' | 'Automatique') {
    this.newModule.update(m => ({ ...m, config: { ...m.config, mode } }));
  }

  updateNewModuleSon(son: boolean) {
    this.newModule.update(m => ({ ...m, config: { ...m.config, son } }));
  }

  addModule() {
    const module = this.newModule();
    
    if (!module.name || module.name.trim() === '') {
      this.addModuleError.set('Le nom est obligatoire');
      return;
    }

    if (!module.ip || module.ip.trim() === '') {
      this.addModuleError.set('L\'adresse IP est obligatoire');
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(module.ip)) {
      this.addModuleError.set('Format d\'adresse IP invalide');
      return;
    }

    this.http.post<Module>(`${this.BASE_URL}/modules`, module).subscribe({
      next: (newModule) => {
        this.modules.update(modules => [...modules, newModule]);
        this.currentPage.set('modules');
        this.addModuleError.set('');
        this.loadDashboardData();
      },
      error: (err) => {
        console.error('Erreur ajout module:', err);
        if (err.error && err.error.error) {
          this.addModuleError.set(err.error.error);
        } else {
          this.addModuleError.set('Erreur lors de l\'ajout du module');
        }
      }
    });
  }

  deleteModule(module: Module) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le module "${module.name}" ?`)) {
      return;
    }

    this.http.delete(`${this.BASE_URL}/modules/${module.id}`).subscribe({
      next: () => {
        this.modules.update(modules => modules.filter(m => m.id !== module.id));
        this.currentPage.set('modules');
        this.selectedModule.set(undefined);
        this.loadDashboardData();
      },
      error: (err) => {
        console.error('Erreur suppression module:', err);
        if (err.error && err.error.error) {
          alert('Erreur: ' + err.error.error);
        } else {
          alert('Erreur lors de la suppression du module');
        }
      }
    });
  }

  setSoundFilter(filter: string) {
    this.soundFilter.set(filter);
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

    this.http.put<Sound>(`${this.BASE_URL}/sounds/${soundId}`, data).subscribe({
      next: (updatedSound) => {
        this.sounds.update(sounds => 
          sounds.map(s => s.id === updatedSound.id ? updatedSound : s)
        );
        this.cancelEditSound();
      },
      error: (err) => {
        console.error('Erreur modification son:', err);
        if (err.error && err.error.error) {
          this.editSoundError.set(err.error.error);
        } else {
          this.editSoundError.set('Erreur lors de la modification du son');
        }
      }
    });
  }
}