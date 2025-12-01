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
import { Module } from '../../../models/interfaces';

@Component({
  selector: 'app-modules-list',
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './modules-list.html',
  styleUrl: './modules-list.css',
})
export class ModulesList implements OnInit {
  private api = inject(ApiService);

  modules = signal<Module[]>([]);
  selectModule = output<Module>();
  addModule = output<void>();

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.api.getModules().subscribe({
      next: (data) => this.modules.set(data),
      error: (err) => console.error('Error loading modules:', err)
    });
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
