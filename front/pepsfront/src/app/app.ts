/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Santiago Alexander RODRIGUEZ TRIANA
 * @description Root application component.
 *
 * Shell layout: sidenav + toolbar + router-outlet.
 * Auth state is managed by AuthService (sessionStorage-persisted).
 * Navigation is handled entirely by Angular Router — no manual page signals.
 *
 * Visibility rules:
 * - The sidenav and toolbar are only shown when authenticated.
 * - /users, /audit-logs, /archive menu items are only shown to admins.
 * - Route guards enforce the same rules at the URL level.
 */
import { Component, computed, signal, inject, PLATFORM_ID } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from './services/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatListModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  private authService = inject(AuthService);
  private router = inject(Router);

  /** True once the session check from sessionStorage has completed (prevents login flash on SSR). */
  isInitialized = computed(() => this.authService.isInitialized());

  /** True when a valid session exists. Controls sidenav/toolbar visibility. */
  isLoggedIn = computed(() => this.authService.isAuthenticated());

  /** True if the current user has the 'admin' role. Controls admin-only menu items. */
  isAdmin = computed(() => this.authService.isAdmin());

  /** Login of the connected user, shown in the toolbar. */
  currentLogin = computed(() => this.authService.currentLogin());

  isSidenavOpen = signal(true);

  toggleSidenav() {
    this.isSidenavOpen.update(open => !open);
  }

  /**
   * Logs the user out, clears the session, and navigates to /login.
   */
  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onMenuKeydown(event: KeyboardEvent, path: string) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.router.navigate([path]);
    }
  }
}