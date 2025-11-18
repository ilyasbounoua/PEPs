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
  
  sounds = signal<Sound[]>([]);

  pageTitle = computed(() => {
    switch(this.currentPage()) {
      case 'dashboard': return 'Tableau de Bord';
      case 'interactions': return 'Historique des Interactions';
      case 'modules': return 'Gestion des Modules';
      case 'module-detail': return `Détail: ${this.selectedModule()?.name || ''}`;
      case 'sounds': return 'Bibliothèque de Sons';
      default: return 'PEP\'S';
    }
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
    this.http.put<Module>(`${this.BASE_URL}/modules/${moduleToSave.id}`, moduleToSave).subscribe({
      next: (updatedModule) => {
        this.modules.update(currentModules =>
          currentModules.map(m => m.id === updatedModule.id ? updatedModule : m)
        );
        console.log('Configuration sauvegardée:', updatedModule);
        this.currentPage.set('modules');
        this.selectedModule.set(undefined);
        this.loadDashboardData();
      },
      error: (err) => {
        console.error('Erreur de sauvegarde:', err);
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
}