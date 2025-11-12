import { Component, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

// --- Données Statiques ---
// Données pour le graphique (simulées)
const dailyData = [
  { time: '8h', count: 12 },
  { time: '10h', count: 18 },
  { time: '12h', count: 25 },
  { time: '14h', count: 21 },
  { time: '16h', count: 15 },
  { time: '18h', count: 8 }
];

// Données pour la table des interactions (simulées)
const interactionsData = [
  { id: 1, date: '2025-01-15 14:23:15', module: 'Module A', type: 'Bec' },
  { id: 2, date: '2025-01-15 13:45:22', module: 'Module B', type: 'Patte' },
  { id: 3, date: '2025-01-15 12:10:08', module: 'Module A', type: 'Bec' },
  { id: 4, date: '2025-01-15 11:30:45', module: 'Module C', type: 'Patte' },
];

// --- Interfaces (Bonne pratique en TypeScript) ---
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
}

interface DailyData {
  time: string;
  count: number;
}

// Données pour les modules (simulées)
// Correction: Déclaration unique avec le bon type 'Module[]'
const modulesData: Module[] = [
  { id: 1, name: 'Module A', location: 'Enclos Nord', status: 'Actif', ip: '192.168.1.10' },
  { id: 2, name: 'Module B', location: 'Enclos Sud', status: 'Actif', ip: '192.168.1.11' },
  { id: 3, name: 'Module C', location: 'Enclos Est', status: 'Inactif', ip: '192.168.1.12' }
];


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule], // Nécessaire pour @if, @for, ngClass, ngStyle
  templateUrl: './app.html', // Utilise le fichier HTML externe
  styleUrls: ['./app.css'],   // Utilise le fichier CSS externe
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  // --- État Global ---
  isLoggedIn = signal(false);
  loginError = signal('');
  // Hash SHA-256 pré-calculé pour le mot de passe "admin"
  readonly correctHash = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918';
  
  currentPage = signal('dashboard');

  // --- État et Données du Dashboard ---
  stats = signal<StatCard>({
    totalInteractions: 156,
    activeModules: 3,
    lastInteraction: '2025-01-15 14:23:15'
  });
  dailyChartData: DailyData[] = dailyData;

  // --- État et Données des Interactions ---
  filter = signal('all');
  interactions: Interaction[] = interactionsData;
  
  // Signal calculé pour filtrer les interactions
  filteredInteractions = computed(() => {
    const f = this.filter();
    // Simule la date d'aujourd'hui pour correspondre aux données
    const todayStr = '2025-01-15'; 

    if (f === 'today') {
      return this.interactions.filter(i => i.date.startsWith(todayStr));
    }
    if (f === 'week') {
      // Logique de la semaine (ici, retourne tout car données limitées)
      return this.interactions;
    }
    return this.interactions; // 'all'
  });

  // --- État et Données des Modules ---
  modules = signal<Module[]>(modulesData);

  // --- Méthodes ---

  /**
   * Vérifie le mot de passe en hachant l'entrée et en la comparant au hash stocké.
   * C'est plus sécurisé que de stocker le mot de passe en clair.
   * Pour une application réelle, le hash 'correctHash' devrait être sur un serveur,
   * et le serveur devrait faire la comparaison.
   */
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
      // 1. Encoder le mot de passe en
      const encoder = new TextEncoder();
      const data = encoder.encode(password);
      
      // 2. Hacher le mot de passe avec SHA-256
      const hashBuffer = await crypto.subtle.digest('SHA-256', data);
      
      // 3. Convertir le buffer en chaîne hexadécimale
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      const hexHash = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

      // 4. Comparer
      if (hexHash === this.correctHash) {
        this.isLoggedIn.set(true);
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
  }

  // Méthode pour le Dashboard
  refreshStats() {
    this.stats.update(currentStats => ({
      ...currentStats,
      totalInteractions: currentStats.totalInteractions + 1
    }));
  }

  // Méthode pour les Interactions
  setFilter(newFilter: string) {
    this.filter.set(newFilter);
  }

  // Méthodes pour les Modules
  toggleModule(moduleId: number) {
    this.modules.update(currentModules => 
      currentModules.map(m => 
        m.id === moduleId 
          ? { ...m, status: m.status === 'Actif' ? 'Inactif' : 'Actif' }
          : m
      )
    );
    
    // Mettre à jour le nombre de modules actifs sur le dashboard
    this.stats.update(currentStats => ({
        ...currentStats,
        activeModules: this.modules().filter(m => m.status === 'Actif').length
    }));
  }

  configureModule(moduleName: string) {
    // Logique de configuration (ex: ouvrir une modale)
    console.log(`Configuration du module: ${moduleName}`);
  }
}