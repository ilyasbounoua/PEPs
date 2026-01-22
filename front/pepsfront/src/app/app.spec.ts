import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { AuthService } from './services/auth';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { signal, computed } from '@angular/core';
import { ApiService } from './services/api';
import { of } from 'rxjs';

class MockAuthService {
  private readonly _isLoggedIn = signal(false);
  private readonly _userId = signal<number | null>(null);
  private readonly _userLogin = signal('');
  private readonly _userRole = signal('');

  isAuthenticated = this._isLoggedIn.asReadonly();
  currentUserId = this._userId.asReadonly();
  currentLogin = this._userLogin.asReadonly();
  currentRole = this._userRole.asReadonly();
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
}


describe('App', () => {
  beforeEach(async () => {
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
    
    // Simulate successful login
    app.onLoginSuccess();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-dashboard')).toBeTruthy();
    expect(compiled.querySelector('app-login')).toBeFalsy();
  });
});