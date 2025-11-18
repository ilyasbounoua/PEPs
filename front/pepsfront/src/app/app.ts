import { Component, ChangeDetectionStrategy, computed, signal, inject, OnInit } from '@angular/core'; // Ajout de inject et OnInit
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http'; // Ajout de HttpClientModule et HttpClient

// --- Importations Angular Material ---
// Importez les modules dont vous avez besoin pour les composants Material
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

// --- Données Statiques (Mocks) ---
const dailyData: DailyData[] = [
  { time: '8h', count: 12 }, { time: '10h', count: 18 }, { time: '12h', count: 25 },
  { time: '14h', count: 21 }, { time: '16h', count: 15 }, { time: '18h', count: 8 }
];

const interactionsData: Interaction[] = [
  { id: 1, date: '2025-01-15 14:23:15', module: 'Module A', type: 'Bec' },
  { id: 2, date: '2025-01-15 13:45:22', module: 'Module B', type: 'Patte' },
  { id: 3, date: '2025-01-15 12:10:08', module: 'Module A', type: 'Bec' },
  { id: 4, date: '2025-01-15 11:30:45', module: 'Module C', type: 'Patte' },
  { id: 5, date: '2025-01-14 09:15:00', module: 'Module B', type: 'Bec' },
];

const modulesData: Module[] = [
  { id: 1, name: 'Module A', location: 'Enclos Nord', status: 'Actif', ip: '192.168.1.10', config: { volume: 80, mode: 'Automatique', actif: true, son: true } },
  { id: 2, name: 'Module B', location: 'Enclos Sud', status: 'Actif', ip: '192.168.1.11', config: { volume: 60, mode: 'Manuel', actif: true, son: false } },
  { id: 3, name: 'Module C', location: 'Enclos Est', status: 'Inactif', ip: '192.168.1.12', config: { volume: 75, mode: 'Automatique', actif: false, son: true } }
];

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule, 
    // --- Modules Material ---
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
  // --- Injection de HttpClient ---
  http = inject(HttpClient);

  // --- État Global ---
  isLoggedIn = signal(false);
  loginError = signal('');
  readonly correctHash = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918';
  
  currentPage = signal('dashboard');
  isSidenavOpen = signal(true); // Menu ouvert par défaut sur desktop

  // --- État et Données du Dashboard ---
  // Initialisation à 0 ou "chargement"
  stats = signal<StatCard>({
    totalInteractions: 0, // Initialisé à 0
    activeModules: modulesData.filter(m => m.status === 'Actif').length,
    lastInteraction: 'Chargement...' // État de chargement
  });
  dailyChartData: DailyData[] = dailyData;

  // --- État et Données des Interactions ---
  filter = signal('all');
  interactions = signal<Interaction[]>(interactionsData);
  displayedColumns: string[] = ['date', 'module', 'type']; // Pour la table Material

  filteredInteractions = computed(() => {
    const f = this.filter();
    const todayStr = '2025-01-15';
    const yesterdayStr = '2025-01-14';

    if (f === 'today') {
      return this.interactions().filter(i => i.date.startsWith(todayStr));
    }
    if (f === 'yesterday') {
      return this.interactions().filter(i => i.date.startsWith(yesterdayStr));
    }
    // "week" - pour cet exemple, on retourne tout
    if (f === 'week') {
      return this.interactions();
    }
    return this.interactions(); // 'all'
  });

  // --- État et Données des Modules ---
  modules = signal<Module[]>(modulesData);
  selectedModule = signal<Module | undefined>(undefined);

  // Titre de la page, calculé
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

  // --- Méthodes ---

  ngOnInit() {
    // Si on est déjà loggé (ex: futur rechargement de page), on charge les données
    // Pour l'instant, la connexion n'est pas persistante,
    // donc cela ne s'activera pas, mais c'est une bonne pratique.
    if (this.isLoggedIn()) {
      this.loadDashboardData();
    }
  }


  loadDashboardData() {
    // On met l'UI en mode chargement
    this.stats.update(current => ({
      ...current,
      totalInteractions: 0,
      lastInteraction: 'Chargement...'
    }));

    // Appel HTTP GET
    this.http.get<StatCard>('http://localhost:8080/PEPs_back/dashboard').subscribe({
      next: (data) => {
        // Succès: on met à jour le signal avec les vraies données
        // Note: L'API devrait retourner la structure StatCard complète
        this.stats.set({
          totalInteractions: data.totalInteractions,
          // Si l'API ne renvoie pas activeModules, on le recalcule
          activeModules: data.activeModules ?? this.modules().filter(m => m.status === 'Actif').length,
          lastInteraction: data.lastInteraction
        });
      },
      error: (err) => {
        console.error('Erreur de chargement du dashboard:', err);
        // En cas d'erreur (ex: backend non démarré), on affiche une erreur
        this.stats.update(current => ({
          ...current,
          totalInteractions: 0,
          lastInteraction: 'Erreur de connexion'
        }));
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
        this.loadDashboardData();
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
    this.loadDashboardData();
  }

  setFilter(newFilter: string) {
    this.filter.set(newFilter);
  }

  selectModule(module: Module) {
    this.selectedModule.set(module);
    this.currentPage.set('module-detail');
  }

  saveModuleConfig(moduleToSave: Module) {
    this.modules.update(currentModules =>
      currentModules.map(m =>
        m.id === moduleToSave.id ? { ...m, ...moduleToSave } : m
      )
    );
    // Mettre à jour le statut global (si 'actif' a changé)
    this.modules.update(currentModules =>
      currentModules.map(m =>
        m.id === moduleToSave.id ? { ...m, status: moduleToSave.config.actif ? 'Actif' : 'Inactif' } : m
      )
    );
    // Mettre à jour les stats du dashboard
    this.stats.update(currentStats => ({
      ...currentStats,
      activeModules: this.modules().filter(m => m.status === 'Actif').length
    }));

    console.log('Configuration sauvegardée:', moduleToSave);
    // Revenir à la liste des modules
    this.currentPage.set('modules');
    this.selectedModule.set(undefined);
  }

  // F6: Export CSV simple
  exportAsCsv() {
    const data = this.filteredInteractions();
    if (data.length === 0) {
      return;
    }

    const headers = ['ID', 'Date', 'Module', 'Type'];
    const rows = data.map(i => [i.id, `"${i.date}"`, i.module, i.type].join(','));
    
    const csvContent = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-s-8;' });
    
    // Créer un lien temporaire et simuler un clic pour télécharger
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'interactions-aras.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  // Helper pour le slider de volume
  formatVolumeLabel(value: number): string {
    return `${value}%`;
  }
}