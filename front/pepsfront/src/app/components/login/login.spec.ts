import { TestBed } from '@angular/core/testing';
import { Login } from './login';
import { AuthService } from '../../services/auth';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { signal } from '@angular/core';

class MockAuthService {
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

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
      ],
    });

    const fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should emit loginSuccess on successful login', async () => {
    const login = 'admin';
    const password = 'PEPS';
    spyOn(authService, 'login').and.callThrough();
    spyOn(component.loginSuccess, 'emit');

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

    expect(authService.login).toHaveBeenCalledWith(login, password);
    expect(component.loginSuccess.emit).toHaveBeenCalled();
  });

  it('should set loginError on failed login', async () => {
    const login = 'admin';
    const password = 'wrongpassword';
    const error = 'Mot de passe incorrect.';
    spyOn(authService, 'login').and.callThrough();

    const mockEvent = {
      target: {
        elements: {
          namedItem: (name:string) => {
            if (name === 'login') return { value: login };
            if (name === 'password') return { value: password };
            return null;
          },
        },
      },
      preventDefault: () => {},
    } as any;

    await component.onSubmit(mockEvent);

    expect(authService.login).toHaveBeenCalledWith(login, password);
    expect(component.loginError()).toBe(error);
  });
});