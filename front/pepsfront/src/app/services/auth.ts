/**
 * AuthService
 * Authentification via backend Spring uniquement.
 * 
 * Système multi-profils :
 * - Stocke userId pour filtrer les données propres à l'utilisateur
 * - Stocke userRole pour contrôler l'accès aux fonctionnalités (ex: gestion users pour admin)
 * - Rôles possibles : "admin", "dauphin", "aras"
 * - Persiste la session dans sessionStorage pour survivre aux refresh
 * 
 * @author Anas EL HOUDI
 */
import { Injectable, signal, inject, computed, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

/** Interface pour la réponse du backend lors du login */
interface LoginResponse {
  message: string;
  userId: number;
  login: string;
  role: string;
  permission: string;
}

/** Clé pour sessionStorage */
const SESSION_KEY = 'peps_auth_session';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);

  // Signaux pour l'état d'authentification
  private readonly isLoggedIn = signal(false);
  private readonly userId = signal<number | null>(null);
  private readonly userLogin = signal<string>('');
  private readonly userRole = signal<string>('');
  private readonly userPermission = signal<string>('');
  // Flag to indicate session check is complete (prevents login flash on SSR)
  private readonly initialized = signal(false);

  // Expositions en lecture seule pour les composants
  isAuthenticated = this.isLoggedIn.asReadonly();
  currentUserId = this.userId.asReadonly();
  currentLogin = this.userLogin.asReadonly();
  currentRole = this.userRole.asReadonly();
  currentUserRole = this.userRole.asReadonly(); // Alias for api.ts
  currentUserPermission = this.userPermission.asReadonly();
  isInitialized = this.initialized.asReadonly();

  // Computed pour vérifier si l'utilisateur est admin
  isAdmin = computed(() => this.userRole() === 'admin');

  // Computed pour vérifier si l'utilisateur a le droit d'éditer
  // Admin role toujours OK. Sinon check permission (editor ou admin)
  canEdit = computed(() => {
    if (this.userRole() === 'admin') return true;
    const perm = this.userPermission();
    return perm === 'editor' || perm === 'admin';
  });

  constructor() {
    // Restore session from sessionStorage on service initialization (browser only)
    this.restoreSession();
  }

  /**
   * Check if running in browser (for SSR compatibility)
   */
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  /**
   * Restore session from sessionStorage if exists (browser only)
   */
  private restoreSession(): void {
    if (!this.isBrowser()) {
      // On server, DON'T mark as initialized - this prevents rendering login page
      // The browser will initialize properly when it hydrates
      return;
    }

    try {
      const stored = sessionStorage.getItem(SESSION_KEY);
      if (stored) {
        const session = JSON.parse(stored);
        this.userId.set(session.userId);
        this.userLogin.set(session.login);
        this.userRole.set(session.role);
        // Fallback for old sessions without permission
        this.userPermission.set(session.permission || 'viewer');
        this.isLoggedIn.set(true);
        console.log('[AuthService] Session restored for user:', session.login, 'Role:', session.role, 'Perm:', session.permission);
      }
    } catch (e) {
      console.error('[AuthService] Failed to restore session:', e);
      this.clearSession();
    }
    // Mark initialization complete (browser only)
    this.initialized.set(true);
  }

  /**
   * Save session to sessionStorage (browser only)
   */
  private saveSession(userId: number, login: string, role: string, permission: string): void {
    if (!this.isBrowser()) return;

    const session = { userId, login, role, permission };
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  }

  /**
   * Clear session from sessionStorage (browser only)
   */
  private clearSession(): void {
    if (!this.isBrowser()) return;

    sessionStorage.removeItem(SESSION_KEY);
  }

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
      this.userPermission.set(response.permission);
      this.isLoggedIn.set(true);

      // Persist to sessionStorage
      this.saveSession(response.userId, response.login, response.role, response.permission);

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
    this.userPermission.set('');
    // Clear persisted session
    this.clearSession();
  }
}

