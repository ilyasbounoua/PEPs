/**
 * @author Santiago Alexander RODRIGUEZ TRIANA
 * @description Unit tests for the App root component after Angular Router migration.
 *
 * Migration notes:
 * - App no longer controls which view to show via a `currentPage` signal.
 * - The shell (sidenav + toolbar) renders only when authenticated.
 * - Route navigation is managed by Angular Router — tests use provideRouter + RouterTestingHarness.
 */
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideLocationMocks } from '@angular/common/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { signal, computed } from '@angular/core';
import { App } from './app';
import { AuthService } from './services/auth';
import { routes } from './app.routes';

class MockAuthService {
  private readonly _isLoggedIn = signal(false);
  private readonly _userId = signal<number | null>(null);
  private readonly _userLogin = signal('');
  private readonly _userRole = signal('');
  private readonly _isInitialized = signal(true);

  isAuthenticated = this._isLoggedIn.asReadonly();
  currentUserId = this._userId.asReadonly();
  currentLogin = this._userLogin.asReadonly();
  currentRole = this._userRole.asReadonly();
  isInitialized = this._isInitialized.asReadonly();
  isAdmin = computed(() => this._userRole() === 'admin');
  canEdit = computed(() => this._userRole() === 'admin');

  login(_user: string, _pass: string) {
    this._isLoggedIn.set(true);
    this._userId.set(1);
    this._userLogin.set('testuser');
    this._userRole.set('admin');
    return Promise.resolve({ success: true });
  }

  logout() {
    this._isLoggedIn.set(false);
    this._userId.set(null);
    this._userLogin.set('');
    this._userRole.set('');
  }

  setLoggedIn(role = 'admin') {
    this._isLoggedIn.set(true);
    this._userId.set(1);
    this._userLogin.set('testuser');
    this._userRole.set(role);
  }
}

describe('App', () => {
  let mockAuth: MockAuthService;

  beforeEach(async () => {
    sessionStorage.clear();
    mockAuth = new MockAuthService();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AuthService, useValue: mockAuth },
        provideRouter(routes),
        provideLocationMocks(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render router-outlet (not login page directly) on initial load', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    // Shell is not rendered when not logged in — only a bare router-outlet
    expect(compiled.querySelector('mat-sidenav-container')).toBeFalsy();
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });

  it('should render sidenav shell when logged in', () => {
    // Simulate authenticated session
    mockAuth.setLoggedIn('admin');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('mat-sidenav-container')).toBeTruthy();
  });

  it('should show admin menu items only when user is admin', () => {
    mockAuth.setLoggedIn('admin');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    // Admin-only nav items should be present
    const navText = compiled.querySelector('mat-nav-list')?.textContent ?? '';
    expect(navText).toContain('Utilisateurs');
    expect(navText).toContain("Journal d'Audit");
    expect(navText).toContain('Archive');
  });

  it('should hide admin menu items for non-admin users', () => {
    mockAuth.setLoggedIn('dauphin');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const navText = compiled.querySelector('mat-nav-list')?.textContent ?? '';
    expect(navText).not.toContain('Utilisateurs');
    expect(navText).not.toContain("Journal d'Audit");
    expect(navText).not.toContain('Archive');
  });

  it('should call authService.logout() and remove sidenav on logout', () => {
    mockAuth.setLoggedIn('admin');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const app = fixture.componentInstance;
    app.logout();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('mat-sidenav-container')).toBeFalsy();
  });
});