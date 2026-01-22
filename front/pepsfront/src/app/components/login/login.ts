import { Component, output, signal, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth';

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
  
  hidePassword = true; 
  loginSuccess = output<void>();
  loginError = signal('');
  
  isReady = signal(false);

  ngOnInit() {
    this.preloadBackground('../../../../public/backgroundlogin.jpg');
  }

  preloadBackground(url: string) {
    // Check if running in the browser
    if (isPlatformBrowser(this.platformId)) {
      const img = new Image();
      img.src = url;
      img.onload = () => this.isReady.set(true);
      img.onerror = () => this.isReady.set(true);
    } else {
      // On the server (SSR), 'Image' doesn't exist.
      // We set ready to true so the server renders the form immediately.
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
      this.loginSuccess.emit();
    } else {
      this.loginError.set(result.error ?? 'Erreur de connexion');
    }
  }
}