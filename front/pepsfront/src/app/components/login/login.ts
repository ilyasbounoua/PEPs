/**
 * @description Login component logic
 */
import { Component, output, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  private authService = inject(AuthService);

  loginSuccess = output<void>();
  loginError = signal('');

  async onSubmit(event: Event) {
    event.preventDefault();
    this.loginError.set('');

    const form = event.target as HTMLFormElement;

    const loginInput = form.elements.namedItem('login') as HTMLInputElement;
    const passwordInput = form.elements.namedItem('password') as HTMLInputElement;

    const login = loginInput.value;
    const password = passwordInput.value;

    const result = await this.authService.login(login, password);

    if (result.success) {
      this.loginSuccess.emit();
    } else {
      this.loginError.set(result.error ?? 'Erreur de connexion');
    }
  }
}
