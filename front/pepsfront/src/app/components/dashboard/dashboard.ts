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
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, MatCardModule, MatIconModule, MatTooltipModule, FormsModule, MatSelectModule, MatFormFieldModule, MatButtonModule],
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

  // Date Filter
  selectedPeriod = 'today';

  // Pagination State
  visibleWeekIndex = 0;

  periods = [
    { value: 'today', label: "Aujourd'hui" },
    { value: 'yesterday', label: 'Hier' },
    { value: 'week', label: '7 derniers jours' },
    { value: 'month', label: '30 derniers jours' }
  ];

  interactionLabel = 'Interactions du jour';
  chartLabel = 'Utilisations du jour';

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

  getPeriodDates(): { start: string, end: string } {
    const end = new Date();
    const start = new Date();

    // Set end of today for consistent range (optional, but good for "inclusive" ranges)
    end.setHours(23, 59, 59, 999);

    switch (this.selectedPeriod) {
      case 'today':
        start.setHours(0, 0, 0, 0);
        break;
      case 'yesterday':
        start.setDate(start.getDate() - 1);
        start.setHours(0, 0, 0, 0);

        // precise end of yesterday
        end.setDate(end.getDate() - 1);
        end.setHours(23, 59, 59, 999);
        break;
      case 'week':
        start.setDate(start.getDate() - 7);
        start.setHours(0, 0, 0, 0);
        break;
      case 'month':
        start.setDate(start.getDate() - 30);
        start.setHours(0, 0, 0, 0);
        break;
    }

    // Format to ISO string (local time preserved if possible, but JS Date.toISOString() is UTC)
    // To match backend SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"), we often need local ISO or handle UTC carefully.
    // Simplest way for local time ISO:
    const toLocalIso = (date: Date) => {
      const offset = date.getTimezoneOffset() * 60000; // offset in milliseconds
      return new Date(date.getTime() - offset).toISOString().slice(0, 19);
    };

    return {
      start: toLocalIso(start),
      end: toLocalIso(end)
    };
  }

  updateLabels() {
    switch (this.selectedPeriod) {
      case 'today':
        this.interactionLabel = 'Interactions du jour';
        this.chartLabel = 'Utilisations du jour';
        break;
      case 'yesterday':
        this.interactionLabel = "Interactions d'hier";
        this.chartLabel = "Utilisations d'hier";
        break;
      case 'week':
        this.interactionLabel = 'Interactions (7 derniers jours)';
        this.chartLabel = 'Utilisations (7 derniers jours)';
        break;
      case 'month':
        this.interactionLabel = 'Interactions (30 derniers jours)';
        this.chartLabel = 'Utilisations (30 derniers jours)';
        break;
    }
  }

  loadData() {
    // Admin uses selectedRole, regular users use their own role via api.ts
    const filterRole = this.isAdmin() ? (this.selectedRole || undefined) : undefined;
    const { start, end } = this.getPeriodDates();

    this.updateLabels();

    this.api.getDashboardStats(filterRole, start, end).subscribe({
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

    this.api.getDailyStats(filterRole, start, end).subscribe({
      next: (data) => {
        this.dailyChartData.set(data);
        this.visibleWeekIndex = 0;
      },
      error: (err) => console.error('Error loading daily stats:', err)
    });
  }

  onProfileChange(role: string) {
    this.selectedRole = role;
    this.loadData();
  }

  formatLabel(time: string): string {
    // time is either "8h", "10h" (from hourly) OR "2023-10-25" (from daily)

    if (this.selectedPeriod === 'today' || this.selectedPeriod === 'yesterday') {
      return time; // Already formatted as "8h" by backend
    }

    // specific handling for daily dates YYYY-MM-DD
    const date = new Date(time);
    if (!isNaN(date.getTime())) {
      // Return DD/MM
      const day = date.getDate().toString().padStart(2, '0');
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      return `${day}/${month}`;
    }

    return time;
  }

  // --- Pagination Logic ---

  // Helper to determine page size based on period
  get pageSize(): number {
    if (this.selectedPeriod === 'today' || this.selectedPeriod === 'yesterday') return 8;
    return 7;
  }

  get paginatedChartData(): DailyData[] {
    const data = this.dailyChartData();

    if (this.selectedPeriod === 'week') {
      return data;
    }

    // For today/yesterday, we only paginate if data.length > 8
    if ((this.selectedPeriod === 'today' || this.selectedPeriod === 'yesterday') && data.length <= 8) {
      return data;
    }

    const pageSize = this.pageSize;
    const totalItems = data.length;

    // Calculate start/end indices from the back (since we want "most recent" first)
    // Page 0 => [total - pageSize, total]

    let endIdx = totalItems - (this.visibleWeekIndex * pageSize);
    let startIdx = endIdx - pageSize;

    // Clamp
    if (endIdx > totalItems) endIdx = totalItems;
    if (endIdx < 0) endIdx = 0;

    if (startIdx < 0) startIdx = 0;

    return data.slice(startIdx, endIdx);
  }

  hasPreviousPage(): boolean {
    if (this.selectedPeriod === 'week') return false;

    const data = this.dailyChartData();
    const pageSize = this.pageSize;

    // If today/yesterday and small dataset, no pagination
    if ((this.selectedPeriod === 'today' || this.selectedPeriod === 'yesterday') && data.length <= 8) return false;

    const totalItems = data.length;
    return ((this.visibleWeekIndex + 1) * pageSize) < totalItems;
  }

  hasNextPage(): boolean {
    if (this.selectedPeriod === 'week') return false;

    const data = this.dailyChartData();
    if ((this.selectedPeriod === 'today' || this.selectedPeriod === 'yesterday') && data.length <= 8) return false;

    return this.visibleWeekIndex > 0;
  }

  previousPage() {
    if (this.hasPreviousPage()) {
      this.visibleWeekIndex++;
    }
  }

  nextPage() {
    if (this.hasNextPage()) {
      this.visibleWeekIndex--;
    }
  }

  getRangeLabel(): string {
    // If 'week', we show full range.
    // If 'month', 'today', 'yesterday' we show range of *visible* data (paginated or not).

    const currentData = this.selectedPeriod === 'week' ? this.dailyChartData() : this.paginatedChartData;

    if (!currentData || currentData.length === 0) return '';

    const start = this.formatLabel(currentData[0].time);
    const end = this.formatLabel(currentData[currentData.length - 1].time);

    // For hourly views (today/yesterday), we might want a separator like " - " instead of "Du ... au ..."
    // Check if labels contains "/" (date) or "h" (hour)
    if (start.includes('h')) {
      return `${start} - ${end}`;
    }

    return `Du ${start} au ${end}`;
  }

  onPeriodChange(period: string) {
    this.selectedPeriod = period;
    this.loadData();
  }
}
