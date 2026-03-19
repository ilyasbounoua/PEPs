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
import { environment } from '../../environments/environment';

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
    // Stage 1: Fast sync check (for UX/UI responsiveness)
    this.restoreFromStorage();
    
    // Stage 2: Ground truth from backend
    if (this.isBrowser()) {
      this.verifySession();
    } else {
      // On server, we consider "initialization" done (it's always unauthenticated by default)
      this.initialized.set(true);
    }
  }

  /**
   * Check if running in browser (for SSR compatibility)
   */
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  /**
   * Fast sync restore (allows near-instant UI rendering if session exists)
   */
  private restoreFromStorage(): void {
    if (!this.isBrowser()) return;
    try {
      const stored = sessionStorage.getItem(SESSION_KEY);
      if (stored) {
        const session = JSON.parse(stored);
        this.populateState(session);
      }
    } catch (e) {
      this.clearSession();
    }
  }

  /**
   * ASYNC initialization: Real session verification with the server.
   */
  private async verifySession(): Promise<void> {
    try {
      const baseUrl = (environment as any).apiUrl;
      const response = await firstValueFrom(
        this.http.get<LoginResponse>(`${baseUrl}/auth/me`, { withCredentials: true })
      );

      // Backend says we are cool -> update signals and sync storage
      this.populateState(response);
      this.saveSession(response.userId, response.login, response.role, response.permission);
      console.log('[AuthService] Session verified with server:', response.login);
    } catch (e) {
      // 401/error -> if we thought we were logged in, we were wrong
      if (this.isLoggedIn()) {
        console.warn('[AuthService] Session invalidated by server.');
        this.logoutLocally();
      }
    } finally {
      // Mark as initialized so Guards and UI can proceed
      this.initialized.set(true);
    }
  }

  /** Helper to populate signals */
  private populateState(data: any): void {
    this.userId.set(data.userId);
    this.userLogin.set(data.login);
    this.userRole.set(data.role);
    this.userPermission.set(data.permission || 'viewer');
    this.isLoggedIn.set(true);
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
      const baseUrl = (environment as any).apiUrl;
      const response = await firstValueFrom(
        this.http.post<LoginResponse>(
          `${baseUrl}/auth/login`,
          { login, password },
          { withCredentials: true }   // needed to receive the HttpOnly jwt cookie
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
    // Fire-and-forget server logout, clear local state immediately
    const baseUrl = (environment as any).apiUrl;
    this.http.post(`${baseUrl}/auth/logout`, {}, { withCredentials: true })
      .subscribe({ error: () => { /* ignore */ } });

    this.logoutLocally();
  }

  /** Clears local signals and storage */
  private logoutLocally(): void {
    this.isLoggedIn.set(false);
    this.userId.set(null);
    this.userLogin.set('');
    this.userRole.set('');
    this.userPermission.set('');
    this.clearSession();
  }
}

