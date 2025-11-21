import { Component, output, signal, inject } from '@angular/core';
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
  selector: 'app-module-form',
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
  templateUrl: './module-form.html',
  styleUrl: './module-form.css',
})
export class ModuleForm {
  private api = inject(ApiService);

  createSuccess = output<void>();
  cancel = output<void>();
  errorMessage = signal('');

  newModule = signal<Omit<Module, 'id'>>({
    name: '',
    location: '',
    status: 'Inactif',
    ip: '',
    config: {
      volume: 50,
      mode: 'Manuel',
      actif: false,
      son: true
    }
  });

  formatVolumeLabel(value: number): string {
    return `${value}%`;
  }

  onCreate() {
    const module = this.newModule();
    this.errorMessage.set('');
    
    if (!module.name || module.name.trim() === '') {
      this.errorMessage.set('Le nom est obligatoire');
      return;
    }

    if (!module.ip || module.ip.trim() === '') {
      this.errorMessage.set('L\'adresse IP est obligatoire');
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(module.ip)) {
      this.errorMessage.set('Format d\'adresse IP invalide');
      return;
    }

    this.api.createModule(module).subscribe({
      next: () => this.createSuccess.emit(),
      error: (err) => {
        console.error('Error creating module:', err);
        if (err.error && err.error.error) {
          this.errorMessage.set(err.error.error);
        } else {
          this.errorMessage.set('Erreur lors de l\'ajout du module');
        }
      }
    });
  }

  onCancel() {
    this.cancel.emit();
  }

  updateName(name: string) {
    this.newModule.update(m => ({ ...m, name }));
  }

  updateIp(ip: string) {
    this.newModule.update(m => ({ ...m, ip }));
  }

  updateActif(actif: boolean) {
    this.newModule.update(m => ({ ...m, config: { ...m.config, actif } }));
  }

  updateVolume(volume: number) {
    this.newModule.update(m => ({ ...m, config: { ...m.config, volume } }));
  }

  updateMode(mode: 'Manuel' | 'Automatique') {
    this.newModule.update(m => ({ ...m, config: { ...m.config, mode } }));
  }

  updateSon(son: boolean) {
    this.newModule.update(m => ({ ...m, config: { ...m.config, son } }));
  }
}
