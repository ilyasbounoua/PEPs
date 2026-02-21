/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the logic for the module detail component, which allows viewing, editing, and deleting a module's configuration.
 */
import { Component, input, output, inject } from '@angular/core';
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
import { AuthService } from '../../../services/auth';
import { Module } from '../../../models/interfaces';
import { I18nService } from '../../../services/i18n';

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
  private authService = inject(AuthService);
  private router = inject(Router);
  private document = inject(DOCUMENT);
  readonly i18n = inject(I18nService);

  readonly canEdit = this.authService.canEdit;

  module = input.required<Module>();
  saveSuccess = output<void>();
  cancel = output<void>();
  deleteSuccess = output<void>();

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

  onSave() {
    const moduleToSave = this.module();

    if (!moduleToSave.name || moduleToSave.name.trim() === '') {
      alert(this.i18n.t('modules.nameRequiredFull'));
      return;
    }

    if (!moduleToSave.ip || moduleToSave.ip.trim() === '') {
      alert(this.i18n.t('modules.ipRequiredFull'));
      return;
    }

    if (!/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(moduleToSave.ip)) {
      alert(this.i18n.t('modules.ipInvalid'));
      return;
    }

    if (moduleToSave.config.volume < 0 || moduleToSave.config.volume > 100) {
      alert(this.i18n.t('modules.volumeRange'));
      return;
    }

    const apiCall = moduleToSave.id
      ? this.api.updateModule(moduleToSave.id, moduleToSave)
      : this.api.createModule(moduleToSave);

    apiCall.subscribe({
      next: () => {
        this.saveSuccess.emit();
        this.navigateToModules();
      },
      error: (err) => {
        console.error('Error saving module:', err);
        if (err.error && err.error.error) {
          alert(this.i18n.t('common.error') + ': ' + err.error.error);
        } else {
          alert(this.i18n.t('modules.saveError'));
        }
      }
    });
  }

  onCancel() {
    this.cancel.emit();
    this.navigateToModules();
  }

  onDelete() {
    const moduleToDelete = this.module();

    if (!confirm(`${this.i18n.t('modules.deleteConfirm')} "${moduleToDelete.name}" ?`)) {
      return;
    }

    this.api.deleteModule(moduleToDelete.id).subscribe({
      next: () => {
        this.deleteSuccess.emit();
        this.navigateToModules();
      },
      error: (err) => {
        console.error('Error deleting module:', err);
        if (err.error && err.error.error) {
          alert(this.i18n.t('common.error') + ': ' + err.error.error);
        } else {
          alert(this.i18n.t('modules.deleteError'));
        }
      }
    });
  }
}
