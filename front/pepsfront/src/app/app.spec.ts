import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { AuthService } from './services/auth';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { signal, computed } from '@angular/core';
import { ApiService } from './services/api';
import { of } from 'rxjs';
import { Module } from './models/interfaces';

class MockAuthService {
  private readonly _isLoggedIn = signal(false);
  private readonly _userId = signal<number | null>(null);
  private readonly _userLogin = signal('');
  private readonly _userRole = signal('');
  private readonly _isInitialized = signal(true); // Mock initialized by default

  isAuthenticated = this._isLoggedIn.asReadonly();
  currentUserId = this._userId.asReadonly();
  currentLogin = this._userLogin.asReadonly();
  currentRole = this._userRole.asReadonly();
  isInitialized = this._isInitialized.asReadonly();
  isAdmin = computed(() => this._userRole() === 'admin');

  login(user: string, pass: string) {
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
}

class MockApiService {
  getDashboardStats() {
    return of({ totalInteractions: 0, totalModules: 0, totalUsedModules: 0 });
  }
  getDailyStats() {
    return of([]);
  }
  getRoles() {
    return of(['admin', 'user']);
  }
}


describe('App', () => {
  beforeEach(async () => {
    // Clear storage to prevent interference from other tests
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        { provide: ApiService, useClass: MockApiService },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations()
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render login page on initial load', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-login')).toBeTruthy();
    expect(compiled.querySelector('app-dashboard')).toBeFalsy();
  });

  it('should render dashboard when logged in', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    // Inject the mock service to manipulate state
    const authService = TestBed.inject(AuthService) as unknown as MockAuthService;

    // Simulate successful login directly on the service (signals are reactive)
    authService.login('test', 'pass');

    // Trigger change detection to update the view based on new signal state
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-dashboard')).toBeTruthy();
    expect(compiled.querySelector('app-login')).toBeFalsy();
  });

  describe('Page Navigation', () => {
    let app: App;
    let fixture: any;

    beforeEach(() => {
      fixture = TestBed.createComponent(App);
      app = fixture.componentInstance;
      const authService = TestBed.inject(AuthService) as unknown as MockAuthService;
      authService.login('test', 'pass');
      fixture.detectChanges();
    });

    it('should change current page', () => {
      app.setCurrentPage('sounds');
      expect(app.currentPage()).toBe('sounds');
    });

    it('should show module detail page on module selection', () => {
      const module: Module = { id: 1, name: 'Test Module', location: 'Test Location', status: 'Actif', ip: '127.0.0.1', config: { volume: 50, mode: 'Manuel', actif: true, son: true } };
      app.onSelectModule(module);
      expect(app.currentPage()).toBe('module-detail');
      expect(app.selectedModule()).toEqual(module);
    });

    it('should show add module page on add module', () => {
      app.onAddModule('dauphin');
      expect(app.currentPage()).toBe('add-module');
    });

    it('should go back to modules page after module saved', () => {
      app.currentPage.set('add-module');
      app.onModuleSaved();
      expect(app.currentPage()).toBe('modules');
    });
  });

  describe('Sidenav', () => {
    it('should toggle sidenav', () => {
      const fixture = TestBed.createComponent(App);
      const app = fixture.componentInstance;
      expect(app.isSidenavOpen()).toBe(true);
      app.toggleSidenav();
      expect(app.isSidenavOpen()).toBe(false);
    });
  });

  describe('Logout', () => {
    it('should log out the user', () => {
      const fixture = TestBed.createComponent(App);
      const app = fixture.componentInstance;
      const authService = TestBed.inject(AuthService) as unknown as MockAuthService;
      authService.login('test', 'pass');
      fixture.detectChanges();

      spyOn(authService, 'logout').and.callThrough();
      app.logout();
      fixture.detectChanges();

      expect(authService.logout).toHaveBeenCalled();
      expect(app.isLoggedIn()).toBe(false);
      expect(app.currentPage()).toBe('dashboard');
    });
  });

  describe('Computed Signals', () => {
    it('should have correct page titles', () => {
      const fixture = TestBed.createComponent(App);
      const app = fixture.componentInstance;

      app.setCurrentPage('dashboard');
      expect(app.pageTitle()).toBe('Tableau de Bord');

      app.setCurrentPage('interactions');
      expect(app.pageTitle()).toBe('Historique des Interactions');

      app.setCurrentPage('modules');
      expect(app.pageTitle()).toBe('Gestion des Modules');

      app.setCurrentPage('sounds');
      expect(app.pageTitle()).toBe('Bibliothèque de Sons');
    });
  });

  describe('Session Storage', () => {
    it('should save and restore current page', () => {
      const fixture = TestBed.createComponent(App);
      const app = fixture.componentInstance;

      app.setCurrentPage('sounds');
      // Manually call the private method for testing
      (app as any).saveCurrentPage('sounds');

      // Create a new component to simulate a page refresh
      const newFixture = TestBed.createComponent(App);
      const newApp = newFixture.componentInstance;

      expect(newApp.currentPage()).toBe('sounds');
    });

    it('should clear current page on logout', () => {
      const fixture = TestBed.createComponent(App);
      const app = fixture.componentInstance;
      spyOn(sessionStorage, 'removeItem');

      app.logout();

      expect(sessionStorage.removeItem).toHaveBeenCalledWith('peps_current_page');
    });
  });
});