import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ModuleDetail } from './module-detail';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Module } from '../../../models/interfaces';
import { ActivatedRoute, Router } from '@angular/router';

class MockApiService {
  getModuleById(id: number) {
    return of(mockModule);
  }
  updateModule(id: number, module: Module) {
    return of(module);
  }
  createModule(module: Module) {
    return of(module);
  }
  deleteModule(id: number) {
    return of(undefined);
  }
}

const mockModule: Module = {
  id: 1,
  name: 'Test Module',
  location: 'Test Location',
  status: 'Actif',
  ip: '1.2.3.4',
  config: {
    volume: 50,
    mode: 'Manuel',
    actif: true,
    son: true,
  },
};

describe('ModuleDetail', () => {
  let component: ModuleDetail;
  let fixture: ComponentFixture<ModuleDetail>;
  let apiService: ApiService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleDetail],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useValue: { canEdit: () => true } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => '1',
              },
            },
          },
        },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModuleDetail);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    router = TestBed.inject(Router);
    // Jasmine spy for router navigation
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should save a module and navigate', () => {
    spyOn(apiService, 'updateModule').and.callThrough();
    component.onSave();
    expect(apiService.updateModule).toHaveBeenCalledWith(mockModule.id, mockModule);
    expect(router.navigate).toHaveBeenCalledWith(['/modules']);
  });

  it('should not save with invalid name', () => {
    spyOn(apiService, 'updateModule').and.callThrough();
    spyOn(window, 'alert');
    const invalidModule = { ...mockModule, name: '' };
    component.module.set(invalidModule);
    fixture.detectChanges();
    component.onSave();
    expect(window.alert).toHaveBeenCalledWith('Le nom du module est obligatoire');
    expect(apiService.updateModule).not.toHaveBeenCalled();
  });

  it("should not save with invalid IP", () => {
    spyOn(apiService, 'updateModule').and.callThrough();
    spyOn(window, 'alert');
    const invalidModule = { ...mockModule, ip: 'invalid-ip' };
    component.module.set(invalidModule);
    fixture.detectChanges();
    component.onSave();
    expect(window.alert).toHaveBeenCalledWith("Format d'adresse IP invalide");
    expect(apiService.updateModule).not.toHaveBeenCalled();
  });

  it('should delete a module and navigate', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'deleteModule').and.callThrough();
    component.onDelete();
    expect(apiService.deleteModule).toHaveBeenCalledWith(mockModule.id);
    expect(router.navigate).toHaveBeenCalledWith(['/modules']);
  });

  it('should navigate on cancel', () => {
    component.onCancel();
    expect(router.navigate).toHaveBeenCalledWith(['/modules']);
  });

  it('should format volume label correctly', () => {
    expect(component.formatVolumeLabel(50)).toBe('50%');
  });
});
