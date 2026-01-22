/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the logic for the dashboard component, which displays statistics and daily data.
 * 
 * Multi-profile system:
 * - Admin can filter by role using dropdown
 * - Regular users see only their own data
 */
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { StatCard, DailyData } from '../../models/interfaces';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, MatCardModule, MatIconModule, MatTooltipModule, FormsModule, MatSelectModule, MatFormFieldModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  readonly isAdmin = this.authService.isAdmin;

  // Admin Filter: role name or empty for all (regular property for ngModel binding)
  selectedRole = '';

  // Profiles for dropdown - loaded from DB
  profiles: { role: string; name: string }[] = [
    { role: '', name: 'Tous les profils' }
  ];

  stats = signal<StatCard>({
    totalInteractions: 0,
    activeModules: 0,
    lastInteraction: 'Chargement...'
  });

  dailyChartData = signal<DailyData[]>([]);

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
    // Admin uses selectedRole, regular users use their own role via api.ts
    const filterRole = this.isAdmin() ? (this.selectedRole || undefined) : undefined;

    this.api.getDashboardStats(filterRole).subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.stats.update(current => ({
          ...current,
          totalInteractions: 0,
          lastInteraction: 'Erreur de connexion'
        }));
      }
    });

    this.api.getDailyStats(filterRole).subscribe({
      next: (data) => this.dailyChartData.set(data),
      error: (err) => console.error('Error loading daily stats:', err)
    });
  }

  onProfileChange(role: string) {
    this.selectedRole = role;
    this.loadData();
  }
}
