import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../environments/environment';

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
      role: 'admin',
      permission: 'admin'
    };

    const promise = service.login(login, password);

    const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
    const req = httpMock.expectOne(`${baseUrl}/auth/login`);
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

    const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
    const req = httpMock.expectOne(`${baseUrl}/auth/login`);
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
      role: 'admin',
      permission: 'admin'
    };
    const promise = service.login(login, password);
    const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
    const req = httpMock.expectOne(`${baseUrl}/auth/login`);
    req.flush(mockResponse);

    // then, logout
    service.logout();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUserId()).toBe(null);
    expect(service.currentLogin()).toBe('');
    expect(service.currentRole()).toBe('');
  });

  describe('AuthService Additional Tests', () => {
    it('should return error if login or password are not provided', async () => {
      let result = await service.login('', 'password');
      expect(result.success).toBe(false);
      expect(result.error).toBe('Login et mot de passe requis.');

      result = await service.login('login', '');
      expect(result.success).toBe(false);
      expect(result.error).toBe('Login et mot de passe requis.');
    });

    it('should restore session from sessionStorage', () => {
      const session = {
        userId: 1,
        login: 'testuser',
        role: 'admin',
        permission: 'admin'
      };
      sessionStorage.setItem('peps_auth_session', JSON.stringify(session));

      // Manually trigger session restoration for the test
      (service as any).restoreSession();

      expect(service.isAuthenticated()).toBe(true);
      expect(service.currentUserId()).toBe(1);
      expect(service.currentLogin()).toBe('testuser');
      expect(service.currentRole()).toBe('admin');
      expect(service.currentUserPermission()).toBe('admin');
    });

    it('should not restore session if sessionStorage is empty', () => {
      sessionStorage.clear();
      // Re-create the service to trigger the constructor
      service = TestBed.inject(AuthService);
      expect(service.isAuthenticated()).toBe(false);
    });

    it('isAdmin should be true for admin user', async () => {
      const login = 'adminuser';
      const password = 'password';
      const mockResponse = {
        message: 'Login successful',
        userId: 1,
        login: 'adminuser',
        role: 'admin',
        permission: 'admin'
      };

      const promise = service.login(login, password);

      const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
      const req = httpMock.expectOne(`${baseUrl}/auth/login`);
      req.flush(mockResponse);
      await promise;

      expect(service.isAdmin()).toBe(true);
    });

    it('isAdmin should be false for non-admin user', async () => {
      const login = 'testuser';
      const password = 'password';
      const mockResponse = {
        message: 'Login successful',
        userId: 2,
        login: 'testuser',
        role: 'dauphin',
        permission: 'viewer'
      };

      const promise = service.login(login, password);

      const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
      const req = httpMock.expectOne(`${baseUrl}/auth/login`);
      req.flush(mockResponse);
      await promise;

      expect(service.isAdmin()).toBe(false);
    });

    it('canEdit should be true for admin user', async () => {
        const login = 'adminuser';
        const password = 'password';
        const mockResponse = {
            message: 'Login successful',
            userId: 1,
            login: 'adminuser',
            role: 'admin',
            permission: 'admin'
        };
        const promise = service.login(login, password);
        const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
        const req = httpMock.expectOne(`${baseUrl}/auth/login`);
        req.flush(mockResponse);
        await promise;
        expect(service.canEdit()).toBe(true);
    });

    it('canEdit should be true for editor user', async () => {
        const login = 'editoruser';
        const password = 'password';
        const mockResponse = {
            message: 'Login successful',
            userId: 2,
            login: 'editoruser',
            role: 'dauphin',
            permission: 'editor'
        };
        const promise = service.login(login, password);
        const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
        const req = httpMock.expectOne(`${baseUrl}/auth/login`);
        req.flush(mockResponse);
        await promise;
        expect(service.canEdit()).toBe(true);
    });

    it('canEdit should be false for viewer user', async () => {
        const login = 'vieweruser';
        const password = 'password';
        const mockResponse = {
            message: 'Login successful',
            userId: 3,
            login: 'vieweruser',
            role: 'aras',
            permission: 'viewer'
        };
        const promise = service.login(login, password);
        const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
        const req = httpMock.expectOne(`${baseUrl}/auth/login`);
        req.flush(mockResponse);
        await promise;
        expect(service.canEdit()).toBe(false);
    });
  });
});