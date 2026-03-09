import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api';
import { AuthService } from './auth';
import { StatCard, DailyData, Interaction, Module, Sound, UserDTO, CreateUserDTO, UpdateUserDTO, AuditLog, ModuleConfig } from '../models/interfaces';
import { environment } from '../../environments/environment';
import { provideHttpClient } from '@angular/common/http';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  let authServiceMock: any;

  const BASE_URL = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';

  beforeEach(() => {
    authServiceMock = {
      currentLogin: jasmine.createSpy('currentLogin').and.returnValue('testuser'),
      currentUserRole: jasmine.createSpy('currentUserRole').and.returnValue('dauphin'),
      isAdmin: jasmine.createSpy('isAdmin').and.returnValue(false)
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock }
      ]
    });

    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('Dashboard', () => {
    it('should get dashboard stats for current user role (non-admin)', () => {
      const mockStats: StatCard = { totalInteractions: 10, activeModules: 5, lastInteraction: '2024-01-01' };
      service.getDashboardStats().subscribe(stats => {
        expect(stats).toEqual(mockStats);
      });
      const req = httpMock.expectOne(`${BASE_URL}/dashboard?role=dauphin`);
      expect(req.request.method).toBe('GET');
      req.flush(mockStats);
    });

    it('should get dashboard stats for a specific role (admin)', () => {
      authServiceMock.isAdmin.and.returnValue(true);
      const mockStats: StatCard = { totalInteractions: 20, activeModules: 10, lastInteraction: '2024-01-02' };
      service.getDashboardStats('aras').subscribe(stats => {
        expect(stats).toEqual(mockStats);
      });
      const req = httpMock.expectOne(`${BASE_URL}/dashboard?role=aras`);
      expect(req.request.method).toBe('GET');
      req.flush(mockStats);
    });

    it('should get daily stats for current user role (non-admin)', () => {
      const mockDailyData: DailyData[] = [{ time: '2024-01-01', count: 5 }, { time: '2024-01-02', count: 10 }];
      service.getDailyStats().subscribe(data => {
        expect(data).toEqual(mockDailyData);
      });
      const req = httpMock.expectOne(`${BASE_URL}/daily-stats?role=dauphin`);
      expect(req.request.method).toBe('GET');
      req.flush(mockDailyData);
    });
  });

  describe('Interactions', () => {
    it('should get interactions for current user role (non-admin)', () => {
      const mockInteractions: any[] = [
        { id: 1, date: '2024-01-01T12:00:00Z', module: 'Module A', type: 'Type A' }
      ];
      const expectedInteractions: Interaction[] = [
        { id: 1, date: '2024-01-01 12:00:00', module: 'Module A', type: 'Type A' }
      ];
      service.getInteractions().subscribe(interactions => {
        expect(interactions).toEqual(expectedInteractions);
      });
      const req = httpMock.expectOne(`${BASE_URL}/interactions?role=dauphin`);
      expect(req.request.method).toBe('GET');
      req.flush(mockInteractions);
    });

    it('should get interactions for a specific role (admin)', () => {
      authServiceMock.isAdmin.and.returnValue(true);
      const mockInteractions: any[] = [
        { id: 2, date: '2024-01-02T12:00:00Z', module: 'Module B', type: 'Type B' }
      ];
      const expectedInteractions: Interaction[] = [
        { id: 2, date: '2024-01-02 12:00:00', module: 'Module B', type: 'Type B' }
      ];
      service.getInteractions('aras').subscribe(interactions => {
        expect(interactions).toEqual(expectedInteractions);
      });
      const req = httpMock.expectOne(`${BASE_URL}/interactions?role=aras`);
      expect(req.request.method).toBe('GET');
      req.flush(mockInteractions);
    });
  });

  describe('Modules', () => {
    const mockConfig: ModuleConfig = { volume: 50, mode: 'Manuel', actif: true, son: true };
    it('should get modules for the current user role', () => {
        const mockModules: Module[] = [
            { id: 1, name: 'Test Module', location: 'Test Location', status: 'Actif', ip: '127.0.0.1', config: mockConfig }
        ];

        service.getModules().subscribe(modules => {
            expect(modules.length).toBe(1);
            expect(modules).toEqual(mockModules);
        });

        const req = httpMock.expectOne(`${BASE_URL}/modules?role=dauphin`);
        expect(req.request.method).toBe('GET');
        req.flush(mockModules);
    });

    it('should create a module for the current user role', () => {
        const newModule: Omit<Module, 'id'> = { name: 'New Module', location: 'New Location', status: 'Actif', ip: '127.0.0.2', config: mockConfig };
        const returnedModule: Module = { id: 2, ...newModule };

        service.createModule(newModule).subscribe(module => {
            expect(module).toEqual(returnedModule);
        });

        const req = httpMock.expectOne(`${BASE_URL}/modules?role=dauphin`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(newModule);
        req.flush(returnedModule);
    });

    it('should update a module', () => {
        const updatedModule: Module = { id: 1, name: 'Updated Module', location: 'Updated Location', status: 'Inactif', ip: '127.0.0.1', config: mockConfig };

        service.updateModule(1, updatedModule).subscribe(module => {
            expect(module).toEqual(updatedModule);
        });

        const req = httpMock.expectOne(`${BASE_URL}/modules/1`);
        expect(req.request.method).toBe('PUT');
        expect(req.request.body).toEqual(updatedModule);
        req.flush(updatedModule);
    });

    it('should delete a module', () => {
        service.deleteModule(1).subscribe(response => {
            expect(response).toBeNull();
        });

        const req = httpMock.expectOne(`${BASE_URL}/modules/1`);
        expect(req.request.method).toBe('DELETE');
        req.flush(null);
    });
  });

  describe('Sounds', () => {
    it('should get sounds for the current user role', () => {
        const mockSounds: Sound[] = [
            { id: 1, name: 'Test Sound', type: 'Test Type', extension: 'mp3', fileName: 'test.mp3' }
        ];

        service.getSounds().subscribe(sounds => {
            expect(sounds.length).toBe(1);
            expect(sounds).toEqual(mockSounds);
        });

        const req = httpMock.expectOne(`${BASE_URL}/sounds?role=dauphin`);
        expect(req.request.method).toBe('GET');
        req.flush(mockSounds);
    });

    it('should upload a sound for the current user role', () => {
        const formData = new FormData();
        formData.append('file', new File([''], 'test.mp3'));
        const returnedSound: Sound = { id: 2, name: 'New Sound', type: 'New Type', extension: 'mp3', fileName: 'new.mp3' };

        service.uploadSound(formData).subscribe(sound => {
            expect(sound).toEqual(returnedSound);
        });

        const req = httpMock.expectOne(`${BASE_URL}/sounds?role=dauphin`);
        expect(req.request.method).toBe('POST');
        req.flush(returnedSound);
    });

    it('should update a sound', () => {
        const updatedSoundData = { name: 'Updated Sound', type: 'Updated Type' };
        const returnedSound: Sound = { id: 1, ...updatedSoundData, extension: 'mp3', fileName: 'test.mp3' };

        service.updateSound(1, updatedSoundData).subscribe(sound => {
            expect(sound).toEqual(returnedSound);
        });

        const req = httpMock.expectOne(`${BASE_URL}/sounds/1`);
        expect(req.request.method).toBe('PUT');
        expect(req.request.body).toEqual(updatedSoundData);
        req.flush(returnedSound);
    });

    it('should delete a sound', () => {
        service.deleteSound(1).subscribe(response => {
            expect(response).toBeNull();
        });

        const req = httpMock.expectOne(`${BASE_URL}/sounds/1`);
        expect(req.request.method).toBe('DELETE');
        req.flush(null);
    });

    it('should get sound file url', () => {
        const url = service.getSoundFileUrl(1);
        expect(url).toBe(`${BASE_URL}/sounds/1/file`);
    });
  });

  describe('Users', () => {
    beforeEach(() => {
        authServiceMock.isAdmin.and.returnValue(true);
    });

    it('should get all users', () => {
        const mockUsers: UserDTO[] = [
            { id: 1, login: 'user1', role: 'dauphin', permission: 'viewer', enabled: true },
            { id: 2, login: 'user2', role: 'aras', permission: 'editor', enabled: false }
        ];

        service.getUsers().subscribe(users => {
            expect(users.length).toBe(2);
            expect(users).toEqual(mockUsers);
        });

        const req = httpMock.expectOne(`${BASE_URL}/users`);
        expect(req.request.method).toBe('GET');
        req.flush(mockUsers);
    });

    it('should get a user by id', () => {
        const mockUser: UserDTO = { id: 1, login: 'user1', role: 'dauphin', permission: 'viewer', enabled: true };

        service.getUserById(1).subscribe(user => {
            expect(user).toEqual(mockUser);
        });

        const req = httpMock.expectOne(`${BASE_URL}/users/1`);
        expect(req.request.method).toBe('GET');
        req.flush(mockUser);
    });

    it('should create a user', () => {
        const newUser: CreateUserDTO = { login: 'newUser', password: 'password', role: 'aras', permission: 'viewer' };
        const returnedUser: UserDTO = { id: 3, ...newUser, enabled: true };

        service.createUser(newUser).subscribe(user => {
            expect(user).toEqual(returnedUser);
        });

        const req = httpMock.expectOne(`${BASE_URL}/users`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(newUser);
        req.flush(returnedUser);
    });

    it('should update a user', () => {
        const updatedUserData: UpdateUserDTO = { login: 'updatedUser' };
        const returnedUser: UserDTO = { id: 1, login: 'updatedUser', role: 'dauphin', permission: 'viewer', enabled: true };

        service.updateUser(1, updatedUserData).subscribe(user => {
            expect(user).toEqual(returnedUser);
        });

        const req = httpMock.expectOne(`${BASE_URL}/users/1`);
        expect(req.request.method).toBe('PUT');
        expect(req.request.body).toEqual(updatedUserData);
        req.flush(returnedUser);
    });

    it('should delete a user', () => {
        service.deleteUser(1).subscribe(response => {
            expect(response).toBeNull();
        });

        const req = httpMock.expectOne(`${BASE_URL}/users/1`);
        expect(req.request.method).toBe('DELETE');
        req.flush(null);
    });

    it('should change a user password', () => {
        service.changePassword(1, 'currentPassword', 'newPassword').subscribe(response => {
            expect(response).toBeTruthy();
        });

        const req = httpMock.expectOne(`${BASE_URL}/users/1/password`);
        expect(req.request.method).toBe('PUT');
        expect(req.request.body).toEqual({ currentPassword: 'currentPassword', newPassword: 'newPassword' });
        req.flush({ success: true });
    });
  });

  describe('Audit Logs', () => {
    beforeEach(() => {
        authServiceMock.isAdmin.and.returnValue(true);
    });

    it('should get all audit logs', () => {
        const mockLogs: AuditLog[] = [
            { id: 1, action: 'CREATE', entityType: 'module', entityId: 1, entityName: 'Test Module', entityRole: 'dauphin', userLogin: 'admin', timestamp: '2024-01-01T12:00:00Z', oldValue: null, newValue: 'New Value', details: 'Module created' }
        ];

        service.getAuditLogs().subscribe(logs => {
            expect(logs).toEqual(mockLogs);
        });

        const req = httpMock.expectOne(`${BASE_URL}/audit-logs`);
        expect(req.request.method).toBe('GET');
        req.flush(mockLogs);
    });

    it('should get audit logs by entity', () => {
        const mockLogs: AuditLog[] = [
            { id: 1, action: 'CREATE', entityType: 'module', entityId: 1, entityName: 'Test Module', entityRole: 'dauphin', userLogin: 'admin', timestamp: '2024-01-01T12:00:00Z', oldValue: null, newValue: 'New Value', details: 'Module created' }
        ];

        service.getAuditLogsByEntity('module').subscribe(logs => {
            expect(logs).toEqual(mockLogs);
        });

        const req = httpMock.expectOne(`${BASE_URL}/audit-logs/by-entity/module`);
        expect(req.request.method).toBe('GET');
        req.flush(mockLogs);
    });

    it('should get audit logs by user', () => {
        const mockLogs: AuditLog[] = [
            { id: 1, action: 'CREATE', entityType: 'module', entityId: 1, entityName: 'Test Module', entityRole: 'dauphin', userLogin: 'admin', timestamp: '2024-01-01T12:00:00Z', oldValue: null, newValue: 'New Value', details: 'Module created' }
        ];

        service.getAuditLogsByUser('admin').subscribe(logs => {
            expect(logs).toEqual(mockLogs);
        });

        const req = httpMock.expectOne(`${BASE_URL}/audit-logs/by-user/admin`);
        expect(req.request.method).toBe('GET');
        req.flush(mockLogs);
    });
  });
});
