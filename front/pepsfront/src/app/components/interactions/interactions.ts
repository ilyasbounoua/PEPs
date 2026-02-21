/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the logic for the interactions component, which displays and filters interactions, and allows exporting them as a CSV file.
 * 
 * Multi-profile system:
 * - Admin can filter by role using dropdown
 * - Regular users see only their own data
 */
import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { Interaction } from '../../models/interfaces';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { I18nService } from '../../services/i18n';

@Component({
  selector: 'app-interactions',
  imports: [CommonModule, FormsModule, MatCardModule, MatRadioModule, MatButtonModule, MatIconModule, MatTableModule, MatSelectModule, MatFormFieldModule],
  templateUrl: './interactions.html',
  styleUrl: './interactions.css',
})
export class Interactions implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  readonly i18n = inject(I18nService);

  readonly isAdmin = this.authService.isAdmin;

  // Admin Filter: role name or empty for all (regular property for ngModel binding)
  selectedRole = '';

  // Profiles for dropdown - loaded from DB (regular array for template iteration)
  profiles: { role: string; name: string }[] = [];

  filter = signal('all');
  interactions = signal<Interaction[]>([]);
  displayedColumns: string[] = ['date', 'module', 'type'];

  filteredInteractions = computed(() => {
    const f = this.filter();
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const weekAgo = new Date(today);
    weekAgo.setDate(weekAgo.getDate() - 7);

    const allInteractions = this.interactions();

    if (f === 'today') {
      return allInteractions.filter(i => new Date(i.date) >= today);
    }
    if (f === 'yesterday') {
      return allInteractions.filter(i => {
        const date = new Date(i.date);
        return date >= yesterday && date < today;
      });
    }
    if (f === 'week') {
      return allInteractions.filter(i => new Date(i.date) >= weekAgo);
    }
    return allInteractions;
  });

  ngOnInit() {
    // Load available roles for admin filter
    if (this.isAdmin()) {
      this.api.getRoles().subscribe({
        next: (roles) => {
          this.profiles = [{ role: '', name: this.i18n.t('common.allProfiles') }];
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

    this.api.getInteractions(filterRole).subscribe({
      next: (data) => this.interactions.set(data),
      error: (err) => console.error('Error loading interactions:', err)
    });
  }

  onProfileChange(role: string) {
    this.selectedRole = role;
    this.loadData();
  }

  setFilter(newFilter: string) {
    this.filter.set(newFilter);
  }

  exportAsCsv() {
    const data = this.filteredInteractions();
    if (data.length === 0) return;

    const headers = ['ID', 'Date', 'Module', 'Type'];
    const rows = data.map(i => [i.id, `"${i.date}"`, i.module, i.type].join(','));

    const csvContent = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });

    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'interactions.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
