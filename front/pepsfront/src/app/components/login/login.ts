import { Component, output, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
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
  
  hidePassword = true; 
  loginSuccess = output<void>();
  loginError = signal('');
  
  // Signal pour masquer l'interface pendant le chargement
  isReady = signal(false);

  ngOnInit() {
    this.preloadBackground('../../../../public/backgroundlogin.jpg');
  }

  preloadBackground(url: string) {
    const img = new Image();
    img.src = url;
    // Dès que l'image est chargée en cache par le navigateur
    img.onload = () => this.isReady.set(true);
    // En cas d'erreur de chemin, on affiche quand même pour ne pas bloquer
    img.onerror = () => this.isReady.set(true);
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