import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    // Clear session storage to ensure clean state
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have isAuthenticated signal initially set to false', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should set isAuthenticated to true on successful login', async () => {
    const login = 'testuser';
    const password = 'password';
    const mockResponse = {
      message: 'Login successful',
      userId: 1,
      login: 'testuser',
      role: 'admin'
    };

    const promise = service.login(login, password);

    const req = httpMock.expectOne('http://localhost:8080/PEPs_back/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);

    const result = await promise;

    expect(result.success).toBe(true);
    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUserId()).toBe(1);
    expect(service.currentLogin()).toBe('testuser');
    expect(service.currentRole()).toBe('admin');
  });

  it('should not set isAuthenticated to true on failed login', async () => {
    const login = 'wronguser';
    const password = 'wrongpassword';

    const promise = service.login(login, password);

    const req = httpMock.expectOne('http://localhost:8080/PEPs_back/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });

    const result = await promise;

    expect(result.success).toBe(false);
    expect(result.error).toBe('Login ou mot de passe incorrect');
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should set isAuthenticated to false on logout', () => {
    // First, log in the user
    const login = 'testuser';
    const password = 'password';
    const mockResponse = {
      message: 'Login successful',
      userId: 1,
      login: 'testuser',
      role: 'admin'
    };
    const promise = service.login(login, password);
    const req = httpMock.expectOne('http://localhost:8080/PEPs_back/auth/login');
    req.flush(mockResponse);

    // then, logout
    service.logout();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUserId()).toBe(null);
    expect(service.currentLogin()).toBe('');
    expect(service.currentRole()).toBe('');
  });
});