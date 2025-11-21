import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../services/api';
import { StatCard, DailyData } from '../../models/interfaces';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, MatCardModule, MatIconModule, MatTooltipModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private api = inject(ApiService);

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
    this.api.getDashboardStats().subscribe({
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

    this.api.getDailyStats().subscribe({
      next: (data) => this.dailyChartData.set(data),
      error: (err) => console.error('Error loading daily stats:', err)
    });
  }
}
