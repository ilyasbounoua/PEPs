/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description Module detail component. Allows viewing, editing, and deleting a module.
 *
 * Migration note: previously received a Module object as @Input from App parent.
 * Now uses ActivatedRoute to read the :id param and fetches the module from the API.
 * Navigation back to /modules is done via Router.navigate().
 */
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
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
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './module-detail.html',
  styleUrl: './module-detail.css',
})
export class ModuleDetail implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly canEdit = this.authService.canEdit;

  module = signal<Module | null>(null);
  isLoading = signal(true);
  loadError = signal('');

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id || isNaN(id)) {
      this.loadError.set('Identifiant de module invalide.');
      this.isLoading.set(false);
      return;
    }

    this.api.getModuleById(id).subscribe({
      next: (mod) => {
        this.module.set(mod);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading module:', err);
        this.loadError.set('Module introuvable ou erreur de chargement.');
        this.isLoading.set(false);
      },
    });
  }

  formatVolumeLabel(value: number): string {
    return `${value}%`;
  }

  onSave() {
    const moduleToSave = this.module();
    if (!moduleToSave) return;

    if (!moduleToSave.name || moduleToSave.name.trim() === '') {
      alert('Le nom du module est obligatoire');
      return;
    }

    if (!moduleToSave.ip || moduleToSave.ip.trim() === '') {
      alert("L'adresse IP est obligatoire");
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(moduleToSave.ip)) {
      alert("Format d'adresse IP invalide");
      return;
    }

    if (moduleToSave.config.volume < 0 || moduleToSave.config.volume > 100) {
      alert('Le volume doit être entre 0 et 100');
      return;
    }

    this.api.updateModule(moduleToSave.id, moduleToSave).subscribe({
      next: () => this.router.navigate(['/modules']),
      error: (err) => {
        console.error('Error saving module:', err);
        alert(err.error?.error ?? 'Erreur lors de la sauvegarde du module');
      },
    });
  }

  onCancel() {
    this.router.navigate(['/modules']);
  }

  onDelete() {
    const moduleToDelete = this.module();
    if (!moduleToDelete) return;

    if (!confirm(`Êtes-vous sûr de vouloir supprimer le module "${moduleToDelete.name}" ?`)) {
      return;
    }

    this.api.deleteModule(moduleToDelete.id).subscribe({
      next: () => this.router.navigate(['/modules']),
      error: (err) => {
        console.error('Error deleting module:', err);
        alert(err.error?.error ?? 'Erreur lors de la suppression du module');
      },
    });
  }
}
