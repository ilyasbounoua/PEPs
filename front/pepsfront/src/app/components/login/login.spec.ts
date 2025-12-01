/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains unit tests for the Login component.
 */
import { TestBed } from '@angular/core/testing';
import { Login } from './login';
import { AuthService } from '../../services/auth';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('Login', () => {
  let component: Login;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    component = TestBed.createComponent(Login).componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should emit loginSuccess on successful login', async () => {
    const password = 'PEPS';
    authService.login.and.returnValue(Promise.resolve({ success: true }));
    spyOn(component.loginSuccess, 'emit');

    const mockEvent = {
      target: {
        elements: {
          namedItem: (name: string) => ({ value: password }),
        },
      },
      preventDefault: () => {},
    } as any;

    await component.onSubmit(mockEvent);

    expect(authService.login).toHaveBeenCalledWith(password);
    expect(component.loginSuccess.emit).toHaveBeenCalled();
  });

  it('should set loginError on failed login', async () => {
    const password = 'wrongpassword';
    const error = 'Mot de passe incorrect.';
    authService.login.and.returnValue(Promise.resolve({ success: false, error }));

    const mockEvent = {
      target: {
        elements: {
          namedItem: (name: string) => ({ value: password }),
        },
      },
      preventDefault: () => {},
    } as any;

    await component.onSubmit(mockEvent);

    expect(authService.login).toHaveBeenCalledWith(password);
    expect(component.loginError()).toBe(error);
  });
});
