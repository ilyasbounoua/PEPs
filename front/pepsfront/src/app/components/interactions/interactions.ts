/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the logic for the interactions component, which displays and filters interactions, and allows exporting them as a CSV file.
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
import { Interaction } from '../../models/interfaces';

@Component({
  selector: 'app-interactions',
  imports: [CommonModule, FormsModule, MatCardModule, MatRadioModule, MatButtonModule, MatIconModule, MatTableModule],
  templateUrl: './interactions.html',
  styleUrl: './interactions.css',
})
export class Interactions implements OnInit {
  private api = inject(ApiService);

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
    this.loadData();
  }

  loadData() {
    this.api.getInteractions().subscribe({
      next: (data) => this.interactions.set(data),
      error: (err) => console.error('Error loading interactions:', err)
    });
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
    link.setAttribute('download', 'interactions-aras.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
