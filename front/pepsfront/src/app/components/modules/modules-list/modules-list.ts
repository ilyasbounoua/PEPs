/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Santiago Alexander RODRIGUEZ TRIANA
 * @description Modules list component. Displays modules filtered by role.
 *
 * Multi-profile system:
 * - Admin can filter by role using dropdown
 * - Regular users see only their own data (API filters by role)
 *
 * Navigation: uses Angular Router — no Output events to parent.
 */
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
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
  private router = inject(Router);

  readonly isAdmin = this.authService.isAdmin;
  readonly canEdit = this.authService.canEdit;

  // Admin Filter: role name or empty for all
  selectedRole = '';

  // Profiles for dropdown - loaded from DB
  profiles: { role: string; name: string }[] = [
    { role: '', name: 'Tous les profils' }
  ];

  modules = signal<Module[]>([]);

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
    this.router.navigate(['/modules', module.id]);
  }

  onAddClick() {
    // Pass the currently selected role as a query param so ModuleForm knows which role to assign
    const extras = this.selectedRole ? { queryParams: { role: this.selectedRole } } : {};
    this.router.navigate(['/modules/new'], extras);
  }

  refreshData() {
    this.loadData();
  }
}
