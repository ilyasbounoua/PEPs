/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the logic for the module detail component, which allows viewing, editing, and deleting a module's configuration.
 */
import { Component, input, output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../services/api';
import { Module } from '../../../models/interfaces';

@Component({
  selector: 'app-module-detail',
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatSliderModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './module-detail.html',
  styleUrl: './module-detail.css',
})
export class ModuleDetail {
  private api = inject(ApiService);

  module = input.required<Module>();
  saveSuccess = output<void>();
  cancel = output<void>();
  deleteSuccess = output<void>();

  formatVolumeLabel(value: number): string {
    return `${value}%`;
  }

  onSave() {
    const moduleToSave = this.module();
    
    if (!moduleToSave.name || moduleToSave.name.trim() === '') {
      alert('Le nom du module est obligatoire');
      return;
    }

    if (!moduleToSave.ip || moduleToSave.ip.trim() === '') {
      alert('L\'adresse IP est obligatoire');
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(moduleToSave.ip)) {
      alert('Format d\'adresse IP invalide');
      return;
    }

    if (moduleToSave.config.volume < 0 || moduleToSave.config.volume > 100) {
      alert('Le volume doit être entre 0 et 100');
      return;
    }

    this.api.updateModule(moduleToSave.id, moduleToSave).subscribe({
      next: () => this.saveSuccess.emit(),
      error: (err) => {
        console.error('Error saving module:', err);
        if (err.error && err.error.error) {
          alert('Erreur: ' + err.error.error);
        } else {
          alert('Erreur lors de la sauvegarde de la configuration');
        }
      }
    });
  }

  onCancel() {
    this.cancel.emit();
  }

  onDelete() {
    const moduleToDelete = this.module();
    
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le module "${moduleToDelete.name}" ?`)) {
      return;
    }

    this.api.deleteModule(moduleToDelete.id).subscribe({
      next: () => this.deleteSuccess.emit(),
      error: (err) => {
        console.error('Error deleting module:', err);
        if (err.error && err.error.error) {
          alert('Erreur: ' + err.error.error);
        } else {
          alert('Erreur lors de la suppression du module');
        }
      }
    });
  }
}
