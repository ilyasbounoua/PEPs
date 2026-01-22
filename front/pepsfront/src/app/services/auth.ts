/**
 * AuthService
 * Authentification via backend Spring uniquement.
 * 
 * Système multi-profils :
 * - Stocke userId pour filtrer les données propres à l'utilisateur
 * - Stocke userRole pour contrôler l'accès aux fonctionnalités (ex: gestion users pour admin)
 * - Rôles possibles : "admin", "dauphin", "aras"
 * 
 * @author Anas EL HOUDI
 */
import { Injectable, signal, inject, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

/** Interface pour la réponse du backend lors du login */
interface LoginResponse {
  message: string;
  userId: number;
  login: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private http = inject(HttpClient);

  // Signaux pour l'état d'authentification
  private readonly isLoggedIn = signal(false);
  private readonly userId = signal<number | null>(null);
  private readonly userLogin = signal<string>('');
  private readonly userRole = signal<string>('');

  // Expositions en lecture seule pour les composants
  isAuthenticated = this.isLoggedIn.asReadonly();
  currentUserId = this.userId.asReadonly();
  currentLogin = this.userLogin.asReadonly();
  currentRole = this.userRole.asReadonly();
  currentUserRole = this.userRole.asReadonly(); // Alias for api.ts

  // Computed pour vérifier si l'utilisateur est admin
  isAdmin = computed(() => this.userRole() === 'admin');

  async login(
    login: string,
    password: string
  ): Promise<{ success: boolean; error?: string }> {

    if (!login || !password) {
      return { success: false, error: 'Login et mot de passe requis.' };
    }

    try {
      // Récupérer la réponse du backend avec userId et role
      const response = await firstValueFrom(
        this.http.post<LoginResponse>(
          'http://localhost:8080/PEPs_back/auth/login',
          { login, password }
        )
      );

      // Stocker les informations utilisateur pour le système multi-profils
      this.userId.set(response.userId);
      this.userLogin.set(response.login);
      this.userRole.set(response.role);
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
    this.userId.set(null);
    this.userLogin.set('');
    this.userRole.set('');
  }
}

