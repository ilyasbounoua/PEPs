import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { AuthService } from './services/auth';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { signal, computed } from '@angular/core';
import { ApiService } from './services/api';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

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
  let router: Router;
  beforeEach(async () => {
    // Clear storage to prevent interference from other tests
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [App, RouterTestingModule],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        { provide: ApiService, useClass: MockApiService },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations()
      ]
    }).compileComponents();
    router = TestBed.inject(Router);
    // use Jasmine spy instead of Jest
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render dashboard when logged in', () => {
    const fixture = TestBed.createComponent(App);
    // Inject the mock service to manipulate state
    const authService = TestBed.inject(AuthService) as unknown as MockAuthService;

    // Simulate successful login directly on the service (signals are reactive)
    authService.login('test', 'pass');

    // Trigger change detection to update the view based on new signal state
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('mat-sidenav-container')).toBeTruthy();
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
    it('should log out the user and navigate to login', () => {
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
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });
  });
});