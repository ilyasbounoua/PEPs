/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the main application component, which acts as the root of the application and manages the overall layout and navigation.
 * 
 * Système multi-profils :
 * - Utilise AuthService pour vérifier le rôle de l'utilisateur
 * - La page "Utilisateurs" n'est visible que pour les admins
 * - Persiste la page courante pour survivre aux refresh
 */
import { Component, computed, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Interactions } from './components/interactions/interactions';
import { ModulesList } from './components/modules/modules-list/modules-list';
import { Sounds } from './components/sounds/sounds';
import { Users } from './components/users/users';
import { Module } from './models/interfaces';
import { ModuleDetail } from './components/modules/module-detail/module-detail';
import { ModuleForm } from './components/modules/module-form/module-form';
import { AuthService } from './services/auth';
import { Account } from './components/account/account';
import { AuditLogsComponent } from './components/audit-logs/audit-logs';

/** Clé pour stocker la page courante */
const PAGE_KEY = 'peps_current_page';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    MatSidenavModule,
    MatListModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    Login,
    Dashboard,
    Interactions,
    ModulesList,
    ModuleDetail,
    ModuleForm,
    Sounds,
    Users,
    Account,
    AuditLogsComponent,
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  private authService = inject(AuthService);
  private platformId = inject(PLATFORM_ID);

  // Use AuthService's isAuthenticated to check login state (survives page refresh)
  isLoggedIn = computed(() => this.authService.isAuthenticated());
  // Track if AuthService has finished checking session (prevents login flash)
  isInitialized = computed(() => this.authService.isInitialized());
  currentPage = signal(this.restoreCurrentPage());
  isSidenavOpen = signal(true);
  selectedModule = signal<Module | undefined>(undefined);
  // Target role for new module/sound creation (admin feature)
  targetRoleForNewModule = signal<string | undefined>(undefined);

  // Vérifie si l'utilisateur connecté est admin (pour afficher la rubrique Users)
  isAdmin = computed(() => this.authService.isAdmin());

  // Login de l'utilisateur connecté (affiché dans la toolbar)
  currentLogin = computed(() => this.authService.currentLogin());

  pageTitle = computed(() => {
    switch (this.currentPage()) {
      case 'dashboard':
        return 'Tableau de Bord';
      case 'interactions':
        return 'Historique des Interactions';
      case 'modules':
        return 'Gestion des Modules';
      case 'module-detail':
        return `Détail: ${this.selectedModule()?.name || ''}`;
      case 'add-module':
        return 'Ajouter un Module';
      case 'sounds':
        return 'Bibliothèque de Sons';
      case 'users':
        return 'Gestion des Utilisateurs';
      case 'account':
        return 'Mon Compte';
      case 'audit-logs':
        return "Journal d'Audit";
      default:
        return "PEP'S";
    }
  });

  /**
   * Check if running in browser (for SSR compatibility)
   */
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  /**
   * Restore current page from sessionStorage (browser only)
   */
  private restoreCurrentPage(): string {
    if (typeof sessionStorage !== 'undefined') {
      try {
        const stored = sessionStorage.getItem(PAGE_KEY);
        if (stored) {
          console.log('[App] Page restored:', stored);
          return stored;
        }
      } catch (e) {
        // Ignore errors on server
      }
    }
    return 'dashboard';
  }

  /**
   * Save current page to sessionStorage (browser only)
   */
  private saveCurrentPage(page: string): void {
    if (!this.isBrowser()) return;
    sessionStorage.setItem(PAGE_KEY, page);
  }

  /**
   * Clear saved page from sessionStorage
   */
  private clearCurrentPage(): void {
    if (!this.isBrowser()) return;
    sessionStorage.removeItem(PAGE_KEY);
  }

  onLoginSuccess() {
    // No longer need to set local signal - AuthService manages state
  }

  setCurrentPage(page: string) {
    this.currentPage.set(page);
    this.selectedModule.set(undefined);
    this.saveCurrentPage(page);  // Persist navigation
  }

  toggleSidenav() {
    this.isSidenavOpen.update((open) => !open);
  }

  onSelectModule(module: Module) {
    this.selectedModule.set(module);
    this.currentPage.set('module-detail');
    this.saveCurrentPage('modules');  // Save 'modules' as fallback (module-detail needs context)
  }

  onAddModule(selectedRole?: string) {
    // Admin must select a specific role before creating a module
    if (this.isAdmin() && !selectedRole) {
      alert('Veuillez sélectionner un profil spécifique avant de créer un module.');
      return;
    }
    this.targetRoleForNewModule.set(selectedRole);
    this.currentPage.set('add-module');
    this.saveCurrentPage('modules');  // Save 'modules' as fallback
  }

  onModuleSaved() {
    this.currentPage.set('modules');
    this.saveCurrentPage('modules');
  }

  /**
   * Déconnexion de l'utilisateur.
   * Réinitialise l'état de l'application et appelle AuthService.logout()
   */
  logout() {
    this.authService.logout();
    this.currentPage.set('dashboard');
    this.clearCurrentPage();  // Clear saved navigation on logout
  }
}
