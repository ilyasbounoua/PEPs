/**
 * AuthService
 * Authentification via backend Spring uniquement
 */
import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private http = inject(HttpClient);

  private readonly isLoggedIn = signal(false);
  isAuthenticated = this.isLoggedIn.asReadonly();

  async login(
    login: string,
    password: string
  ): Promise<{ success: boolean; error?: string }> {

    if (!login || !password) {
      return { success: false, error: 'Login et mot de passe requis.' };
    }

    try {
      await firstValueFrom(
        this.http.post(
          'http://localhost:8080/PEPs_back/auth/login',
          { login, password }
        )
      );

      this.isLoggedIn.set(true);
      return { success: true };

    } catch (err) {
      return {
        success: false,
        error: 'Login ou mot de passe incorrect'
      };
    }
  }

  logout(): void {
    this.isLoggedIn.set(false);
  }
}
