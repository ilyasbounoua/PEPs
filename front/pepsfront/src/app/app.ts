/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI, Santiago Alexander RODRIGUEZ TRIANA
 * @description This file contains the main application component, which acts as the root of the application and manages the overall layout and navigation.
 * 
 * Shell layout: sidenav + toolbar + router-outlet.
 * Auth state is managed by AuthService (sessionStorage-persisted).
 * Navigation is handled entirely by Angular Router.
 * 
 * Système multi-profils :
 * - Utilise AuthService pour vérifier le rôle de l'utilisateur
 * - La page "Utilisateurs" n'est visible que pour les admins
 */
import { Component, computed, signal, inject, PLATFORM_ID, effect } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from './services/auth';
import { I18nService } from './services/i18n';
import { NotificationService } from './services/notification.service';
import { ToastOverlayComponent } from './components/toast-overlay/toast-overlay';

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
    MatTooltipModule,
    ToastOverlayComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  private authService = inject(AuthService);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private platformId = inject(PLATFORM_ID);
  readonly i18n = inject(I18nService);

  isSidenavOpen = signal(true);
  currentPage = signal('dashboard');

  constructor() {
    // Listen to router events to update currentPage (for title calculation)
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.urlAfterRedirects;
      if (url.includes('/dashboard')) this.currentPage.set('dashboard');
      else if (url.includes('/interactions')) this.currentPage.set('interactions');
      else if (url.includes('/modules/new')) this.currentPage.set('add-module');
      else if (url.includes('/modules/')) this.currentPage.set('module-detail');
      else if (url.includes('/modules')) this.currentPage.set('modules');
      else if (url.includes('/sounds')) this.currentPage.set('sounds');
      else if (url.includes('/users')) this.currentPage.set('users');
      else if (url.includes('/account')) this.currentPage.set('account');
      else if (url.includes('/audit-logs')) this.currentPage.set('audit-logs');
      else if (url.includes('/archive')) this.currentPage.set('archive');
      else if (url.includes('/notifications')) this.currentPage.set('notifications');
    });

    effect(() => {
      if (this.isLoggedIn() && this.authService.isAdmin()) {
        // Set navigation callback for toast clicks
        this.notificationService.onNavigateToNotifications = () => {
          this.notificationService.dismissAllToasts();
          this.router.navigate(['/notifications']);
        };
        // Trigger welcome toast only once on fresh login
        this.notificationService.showWelcomeToastIfNeeded();
        // Start polling for newly arriving offline notifications
        this.notificationService.startPolling();
      } else {
        this.notificationService.onNavigateToNotifications = null;
        this.notificationService.stopPolling();
      }
    });
  }

  /** True once the session check from sessionStorage has completed (prevents login flash on SSR). */
  isInitialized = computed(() => this.authService.isInitialized());

  /** True when a valid session exists. Controls sidenav/toolbar visibility. */
  isLoggedIn = computed(() => this.authService.isAuthenticated());

  /** True if the current user has the 'admin' role. Controls admin-only menu items. */
  isAdmin = computed(() => this.authService.isAdmin());

  /** Login of the connected user, shown in the toolbar. */
  currentLogin = computed(() => this.authService.currentLogin());

  pageTitle = computed(() => {
    switch (this.currentPage()) {
      case 'dashboard':
        return this.i18n.t('pageTitles.dashboard');
      case 'interactions':
        return this.i18n.t('pageTitles.interactions');
      case 'modules':
        return this.i18n.t('pageTitles.modules');
      case 'module-detail':
        return this.i18n.t('pageTitles.moduleDetail');
      case 'add-module':
        return this.i18n.t('pageTitles.addModule');
      case 'sounds':
        return this.i18n.t('pageTitles.sounds');
      case 'users':
        return this.i18n.t('pageTitles.users');
      case 'account':
        return this.i18n.t('pageTitles.account');
      case 'audit-logs':
        return this.i18n.t('pageTitles.auditLogs');
      case 'archive':
        return this.i18n.t('pageTitles.archive');
      case 'notifications':
        return this.i18n.t('pageTitles.notifications');
      default:
        return this.i18n.t('pageTitles.default');
    }
  });

  /**
   * Check if running in browser (for SSR compatibility)
   */
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  toggleSidenav() {
    this.isSidenavOpen.update((open) => !open);
  }

  /**
   * Logs the user out, clears the session, and navigates to /login.
   */
  logout() {
    this.authService.logout();
    this.notificationService.stopPolling();
    this.notificationService.resetWelcomeFlag();
    this.router.navigate(['/login']);
    if (this.isBrowser()) {
      sessionStorage.removeItem('peps_archive_warning_dismissed');
    }
  }

  /**
   * Toggle language between FR and EN.
   * Saves preference to database if user is logged in.
   */
  toggleLang() {
    const newLang = this.i18n.toggle();
    const userId = this.authService.currentUserId();
    if (userId) {
      this.i18n.saveToDatabase(userId, newLang);
    }
  }
}
