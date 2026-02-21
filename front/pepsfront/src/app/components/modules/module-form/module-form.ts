/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the logic for the module form component, which allows creating a new module.
 */
import { Component, output, signal, inject, input } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../services/api';
import { Module } from '../../../models/interfaces';
import { I18nService } from '../../../services/i18n';

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
  private router = inject(Router);
  private document = inject(DOCUMENT);
  readonly i18n = inject(I18nService);

  // Input: target role for the new module (passed by parent when admin selects a filter)
  targetRole = input<string | undefined>(undefined);

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

  private navigateToModules() {
    // Simulate click on "Modules" link in navigation
    console.log('Attempting navigation to Modules...');
    const navItems = this.document.querySelectorAll('.mat-list-item, .mat-mdc-list-item');
    let clicked = false;
    for (let i = 0; i < navItems.length; i++) {
      const item = navItems[i] as HTMLElement;
      if (item.textContent?.includes('Modules')) {
        item.click();
        clicked = true;
        break;
      }
    }

    if (!clicked) {
      console.log('Modules link not found, forced navigation to /');
      this.router.navigate(['/'], { onSameUrlNavigation: 'reload' });
    }
  }

  onCreate() {
    const module = this.newModule();
    const role = this.targetRole();
    this.errorMessage.set('');

    // If role is undefined and api service determines user is admin, show error
    // (admin must select a specific role before creating a module)
    if (role === undefined) {
      // Check if this is an admin without a selected role
      // The api service will handle non-admin users correctly
      // For safety, we just pass undefined and let api handle it
    }

    if (!module.name || module.name.trim() === '') {
      this.errorMessage.set(this.i18n.t('modules.nameRequired'));
      return;
    }

    if (!module.ip || module.ip.trim() === '') {
      this.errorMessage.set(this.i18n.t('modules.ipRequired'));
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(module.ip)) {
      this.errorMessage.set(this.i18n.t('modules.ipInvalid'));
      return;
    }

    this.api.createModule(module, role).subscribe({
      next: () => {
        this.createSuccess.emit();
        this.navigateToModules();
      },
      error: (err) => {
        console.error('Error creating module:', err);
        if (err.error && err.error.error) {
          this.errorMessage.set(err.error.error);
        } else {
          this.errorMessage.set(this.i18n.t('modules.addError'));
        }
      }
    });
  }

  onCancel() {
    this.cancel.emit();
    this.navigateToModules();
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
