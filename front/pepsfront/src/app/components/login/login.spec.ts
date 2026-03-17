/**
 * @author Santiago Alexander RODRIGUEZ TRIANA
 * @description Unit tests for the Login component after Angular Router migration.
 *
 * Migration notes:
 * - The `loginSuccess` @Output no longer exists — on success, the component
 *   calls `this.router.navigate(['/dashboard'])` instead.
 * - Tests now spy on Router.navigate to verify successful login navigation.
 */
import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Login } from './login';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

class MockAuthService {
  isAuthenticated = signal(false);
  currentUserId = signal<number | null>(null);
  currentLogin = signal('');
  currentRole = signal('');
  currentUserRole = signal('');
  currentUserPermission = signal('');
  isInitialized = signal(true);
  isAdmin = signal(false);
  canEdit = signal(false);

  login(user: string, pass: string): Promise<{ success: boolean; error?: string }> {
    if (pass === 'PEPS') {
      return Promise.resolve({ success: true });
    } else {
      return Promise.resolve({ success: false, error: 'Mot de passe incorrect.' });
    }
  }
}

describe('Login', () => {
  let component: Login;
  let authService: AuthService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideRouter([]),
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to /dashboard on successful login', async () => {
    const navigateSpy = spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true));
    spyOn(authService, 'login').and.callThrough();

    const mockEvent = {
      target: {
        elements: {
          namedItem: (name: string) => {
            if (name === 'login') return { value: 'admin' };
            if (name === 'password') return { value: 'PEPS' };
            return null;
          },
        },
      },
      preventDefault: () => { },
    } as any;

    await component.onSubmit(mockEvent);

    expect(authService.login).toHaveBeenCalledWith('admin', 'PEPS');
    expect(navigateSpy).toHaveBeenCalledWith('/dashboard');
  });

  it('should set loginError on failed login', async () => {
    spyOn(authService, 'login').and.callThrough();

    const mockEvent = {
      target: {
        elements: {
          namedItem: (name: string) => {
            if (name === 'login') return { value: 'admin' };
            if (name === 'password') return { value: 'wrongpassword' };
            return null;
          },
        },
      },
      preventDefault: () => { },
    } as any;

    await component.onSubmit(mockEvent);

    expect(authService.login).toHaveBeenCalledWith('admin', 'wrongpassword');
    expect(component.loginError()).toBe('Mot de passe incorrect.');
  });

  it('should toggle hidePassword', () => {
    expect(component.hidePassword).toBe(true);
    component.hidePassword = !component.hidePassword;
    expect(component.hidePassword).toBe(false);
  });

  it('should call preloadBackground on init', () => {
    spyOn(component, 'preloadBackground');
    component.ngOnInit();
    expect(component.preloadBackground).toHaveBeenCalledWith('/backgroundlogin.jpg');
  });

  it('should clear loginError on submit', async () => {
    component.loginError.set('Previous error');
    const login = 'admin';
    const password = 'PEPS';
    spyOn(authService, 'login').and.callThrough();

    const mockEvent = {
      target: {
        elements: {
          namedItem: (name: string) => {
            if (name === 'login') return { value: login };
            if (name === 'password') return { value: password };
            return null;
          },
        },
      },
      preventDefault: () => {},
    } as any;

    await component.onSubmit(mockEvent);

    expect(component.loginError()).toBe('');
  });
});