import { Component, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth';
import { Router, ActivatedRoute } from '@angular/router';

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
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  hidePassword = true;
  loginError = signal('');
  isReady = signal(false);

  ngOnInit() {
    this.preloadBackground('/backgroundlogin.jpg');

    // If we're already authenticated, redirect to the intended page or dashboard.
    if (this.authService.isAuthenticated()) {
      const returnUrl = this.getSafeReturnUrl();
      this.router.navigateByUrl(returnUrl);
    }
  }

  /**
   * Prevents "Open Redirect" vulnerabilities by ensuring the returnUrl 
   * is a relative path within our application.
   */
  private getSafeReturnUrl(): string {
    const url = this.route.snapshot.queryParams['returnUrl'];
    if (url && url.startsWith('/') && !url.startsWith('//')) {
      return url;
    }
    return '/dashboard';
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
      const returnUrl = this.getSafeReturnUrl();
      this.router.navigateByUrl(returnUrl);
    } else {
      this.loginError.set(result.error ?? 'Erreur de connexion');
    }
  }
}