import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private isLoggedIn = signal(false);
  private readonly correctHash = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918';

  isAuthenticated = this.isLoggedIn.asReadonly();

  async login(password: string): Promise<{ success: boolean; error?: string }> {
    if (!password) {
      return { success: false, error: 'Veuillez entrer un mot de passe.' };
    }

    try {
      const encoder = new TextEncoder();
      const data = encoder.encode(password);
      const hashBuffer = await crypto.subtle.digest('SHA-256', data);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      const hexHash = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

      if (hexHash === this.correctHash) {
        this.isLoggedIn.set(true);
        return { success: true };
      } else {
        return { success: false, error: 'Mot de passe incorrect.' };
      }
    } catch (err) {
      console.error('Erreur de hachage:', err);
      return { success: false, error: 'Une erreur est survenue.' };
    }
  }

  logout(): void {
    this.isLoggedIn.set(false);
  }
}
