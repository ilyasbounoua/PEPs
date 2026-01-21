/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the main application component, which acts as the root of the application and manages the overall layout and navigation.
 * 
 * Système multi-profils :
 * - Utilise AuthService pour vérifier le rôle de l'utilisateur
 * - La page "Utilisateurs" n'est visible que pour les admins
 */
import { Component, computed, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
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

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
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
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  private authService = inject(AuthService);

  isLoggedIn = signal(false);
  currentPage = signal('dashboard');
  isSidenavOpen = signal(true);
  selectedModule = signal<Module | undefined>(undefined);

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
      default:
        return "PEP'S";
    }
  });

  onLoginSuccess() {
    this.isLoggedIn.set(true);
  }

  setCurrentPage(page: string) {
    this.currentPage.set(page);
    this.selectedModule.set(undefined);
  }

  toggleSidenav() {
    this.isSidenavOpen.update((open) => !open);
  }

  onSelectModule(module: Module) {
    this.selectedModule.set(module);
    this.currentPage.set('module-detail');
  }

  onAddModule() {
    this.currentPage.set('add-module');
  }

  onModuleSaved() {
    this.currentPage.set('modules');
  }

  /**
   * Déconnexion de l'utilisateur.
   * Réinitialise l'état de l'application et appelle AuthService.logout()
   */
  logout() {
    this.authService.logout();
    this.isLoggedIn.set(false);
    this.currentPage.set('dashboard');
  }
}
