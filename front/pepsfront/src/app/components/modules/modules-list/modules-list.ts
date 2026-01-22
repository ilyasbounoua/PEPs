/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the logic for the modules list component, which displays a list of modules and allows selecting or adding a module.
 * 
 * Multi-profile system:
 * - Admin can filter by role using dropdown
 * - Regular users see only their own data
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

  // Admin Filter: role name or empty for all (regular property for ngModel binding)
  selectedRole = '';

  // Profiles for dropdown - loaded from DB (regular array for template iteration)
  profiles: { role: string; name: string }[] = [
    { role: '', name: 'Tous les profils' }
  ];

  modules = signal<Module[]>([]);
  selectModule = output<Module>();
  addModule = output<void>();

  ngOnInit() {
    // Load available roles for admin filter
    if (this.isAdmin()) {
      this.api.getRoles().subscribe({
        next: (roles) => {
          this.profiles = [{ role: '', name: 'Tous les profils' }];
          roles.forEach(r => this.profiles.push({ role: r, name: r.charAt(0).toUpperCase() + r.slice(1) }));
        },
        error: (err) => console.error('Error loading roles:', err)
      });
    }
    this.loadData();
  }

  loadData() {
    const filterRole = this.isAdmin() ? (this.selectedRole || undefined) : undefined;

    this.api.getModules(filterRole).subscribe({
      next: (data) => this.modules.set(data),
      error: (err) => console.error('Error loading modules:', err)
    });
  }

  onProfileChange(role: string) {
    this.selectedRole = role;
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
