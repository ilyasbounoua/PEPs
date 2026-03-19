import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth';
import { ApiService } from '../../services/api';
import { I18nService } from '../../services/i18n';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  private authService = inject(AuthService);
  private api = inject(ApiService);
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  readonly i18n = inject(I18nService);

  hidePassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;
  loginError = signal('');
  isReady = signal(false);

  // Reset password mode
  showResetForm = signal(false);
  resetDone = signal(false); // true after successful reset — hides form fields
  resetLogin = signal('');
  resetNewPassword = signal('');
  resetConfirmPassword = signal('');
  resetError = signal('');
  resetSuccess = signal('');
  isResetting = signal(false);
  redirectCountdown = signal(0);

  ngOnInit() {
    this.preloadBackground('/backgroundlogin.jpg');
  }

  preloadBackground(url: string) {
    if (isPlatformBrowser(this.platformId)) {
      const img = new Image();
      img.src = url;
      img.onload = () => this.isReady.set(true);
      img.onerror = () => this.isReady.set(true);
    } else {
      this.isReady.set(true);
    }
  }

  async onSubmit(event: Event) {
    event.preventDefault();
    this.loginError.set('');
    const form = event.target as HTMLFormElement;
    const loginInput = form.elements.namedItem('login') as HTMLInputElement;
    const passwordInput = form.elements.namedItem('password') as HTMLInputElement;

    const result = await this.authService.login(loginInput.value, passwordInput.value);

    if (result.success) {
      this.router.navigate(['/dashboard']);
    } else {
      this.loginError.set(result.error ?? this.i18n.t('login.error'));
    }
  }

  showReset() {
    this.showResetForm.set(true);
    this.resetDone.set(false);
    this.resetError.set('');
    this.resetSuccess.set('');
    this.resetLogin.set('');
    this.resetNewPassword.set('');
    this.resetConfirmPassword.set('');
    this.loginError.set('');
    this.redirectCountdown.set(0);
  }

  backToLogin() {
    this.showResetForm.set(false);
    this.resetDone.set(false);
    this.resetError.set('');
    this.resetSuccess.set('');
    this.redirectCountdown.set(0);
  }

  submitReset() {
    this.resetError.set('');
    this.resetSuccess.set('');

    const login = this.resetLogin().trim();
    const newPwd = this.resetNewPassword();
    const confirmPwd = this.resetConfirmPassword();

    if (!login) {
      this.resetError.set(this.i18n.t('login.resetLoginRequired'));
      return;
    }
    if (!newPwd) {
      this.resetError.set(this.i18n.t('login.resetPasswordRequired'));
      return;
    }
    if (newPwd.length < 4) {
      this.resetError.set(this.i18n.t('login.resetPasswordMinLength'));
      return;
    }
    if (newPwd !== confirmPwd) {
      this.resetError.set(this.i18n.t('login.resetPasswordMismatch'));
      return;
    }

    this.isResetting.set(true);

    this.api.resetPassword(login, newPwd).subscribe({
      next: () => {
        this.isResetting.set(false);
        this.resetDone.set(true);
        this.resetSuccess.set(this.i18n.t('login.resetSuccess'));
        this.startRedirectCountdown();
      },
      error: () => {
        this.isResetting.set(false);
        this.resetError.set(this.i18n.t('login.resetError'));
      }
    });
  }

  private startRedirectCountdown() {
    this.redirectCountdown.set(5);
    const interval = setInterval(() => {
      const current = this.redirectCountdown();
      if (current <= 1) {
        clearInterval(interval);
        this.backToLogin();
      } else {
        this.redirectCountdown.set(current - 1);
      }
    }, 1000);
  }
}