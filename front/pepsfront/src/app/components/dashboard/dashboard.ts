/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the logic for the dashboard component, which displays statistics and daily data.
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
  private readonly authService = inject(AuthService); // Inject AuthService

  readonly isAdmin = this.authService.isAdmin;

  // Admin Filter: 1=Admin(All), 2=Aras, 3=Dauphin
  selectedProfile = signal<number>(1);

  // Profiles for dropdown
  profiles = [
    { id: 1, name: 'Tous les profils' },
    { id: 2, name: 'Aras' },
    { id: 3, name: 'Dauphin' }
  ];

  stats = signal<StatCard>({
    totalInteractions: 0,
    activeModules: 0,
    lastInteraction: 'Chargement...'
  });

  dailyChartData = signal<DailyData[]>([]);

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

    this.api.getDashboardStats(profileId).subscribe({
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

    this.api.getDailyStats(profileId).subscribe({ // Assuming getDailyStats will update next
      next: (data) => this.dailyChartData.set(data),
      error: (err) => console.error('Error loading daily stats:', err)
    });
  }

  onProfileChange() {
    this.loadData();
  }
}
