/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the main application component, which acts as the root of the application and manages the overall layout and navigation.
 */
import { Component, computed, signal } from '@angular/core';
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
import { Module } from './models/interfaces';
import { ModuleDetail } from './components/modules/module-detail/module-detail';
import { ModuleForm } from './components/modules/module-form/module-form';

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
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  isLoggedIn = signal(false);
  currentPage = signal('dashboard');
  isSidenavOpen = signal(true);
  selectedModule = signal<Module | undefined>(undefined);

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
}
