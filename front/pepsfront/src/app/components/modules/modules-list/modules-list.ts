/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the logic for the modules list component, which displays a list of modules and allows selecting or adding a module.
 */
import { Component, OnInit, output, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
import { Module } from '../../../models/interfaces';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-modules-list',
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, FormsModule, MatSelectModule, MatFormFieldModule],
  templateUrl: './modules-list.html',
  styleUrl: './modules-list.css',
})
export class ModulesList implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);

  readonly isAdmin = this.authService.isAdmin;

  // Admin Filter: 1=Admin(All), 2=Aras, 3=Dauphin
  selectedProfile = signal<number>(1);

  // Profiles for dropdown
  profiles = [
    { id: 1, name: 'Tous les profils' },
    { id: 2, name: 'Aras' },
    { id: 3, name: 'Dauphin' }
  ];

  modules = signal<Module[]>([]);
  selectModule = output<Module>();
  addModule = output<void>();

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    let profileId: number | undefined = undefined;

    if (this.isAdmin()) {
      const selected = this.selectedProfile();
      if (selected !== 1) {
        profileId = selected;
      }
    }

    this.api.getModules(profileId).subscribe({
      next: (data) => this.modules.set(data),
      error: (err) => console.error('Error loading modules:', err)
    });
  }

  onProfileChange() {
    this.loadData();
  }

  onModuleClick(module: Module) {
    this.selectModule.emit(module);
  }

  onAddClick() {
    this.addModule.emit();
  }

  refreshData() {
    this.loadData();
  }
}
