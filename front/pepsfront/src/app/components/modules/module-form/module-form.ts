/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description Module creation form component.
 *
 * Migration note: previously received targetRole as @Input from App parent.
 * Now reads the role from the ?role= query param via ActivatedRoute.
 * Navigation back to /modules is done via Router.navigate().
 * The DOM-clicking navigateToModules() hack has been removed.
 */
import { Component, signal, inject } from '@angular/core';
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
    MatButtonModule,
  ],
  templateUrl: './module-form.html',
  styleUrl: './module-form.css',
})
export class ModuleForm {
  private api = inject(ApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  readonly i18n = inject(I18nService);

  /**
   * Role assigned to the new module.
   * Read from the ?role= query param (set by ModulesList when admin selects a filter).
   * Undefined for non-admin users — ApiService uses their own role in that case.
   */
  private readonly targetRole: string | undefined =
    this.route.snapshot.queryParamMap.get('role') ?? undefined;

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
      son: true,
    },
  });

  formatVolumeLabel(value: number): string {
    return `${value}%`;
  }

  onCreate() {
    const module = this.newModule();
    this.errorMessage.set('');

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

    this.api.createModule(module, this.targetRole).subscribe({
      next: () => this.router.navigate(['/modules']),
      error: (err: any) => {
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
    this.router.navigate(['/modules']);
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
