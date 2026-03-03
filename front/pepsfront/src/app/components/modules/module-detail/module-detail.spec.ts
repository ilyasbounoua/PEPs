import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ModuleDetail } from './module-detail';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Module } from '../../../models/interfaces';

class MockApiService {
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

describe('ModuleDetail', () => {
  let component: ModuleDetail;
  let fixture: ComponentFixture<ModuleDetail>;
  let apiService: ApiService;

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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleDetail],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useValue: { canEdit: () => true } },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModuleDetail);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    fixture.componentRef.setInput('module', mockModule);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should save a module and emit success', () => {
    spyOn(apiService, 'updateModule').and.callThrough();
    spyOn(component.saveSuccess, 'emit');
    component.onSave();
    expect(apiService.updateModule).toHaveBeenCalledWith(mockModule.id, mockModule);
    expect(component.saveSuccess.emit).toHaveBeenCalled();
  });

  it('should show alert on save with invalid name', () => {
    spyOn(window, 'alert');
    const invalidModule = { ...mockModule, name: '' };
    fixture.componentRef.setInput('module', invalidModule);
    fixture.detectChanges();
    component.onSave();
    expect(window.alert).toHaveBeenCalledWith('Le nom du module est obligatoire');
  });

  it("should show alert on save with invalid IP", () => {
    spyOn(window, 'alert');
    const invalidModule = { ...mockModule, ip: 'invalid-ip' };
    fixture.componentRef.setInput('module', invalidModule);
    fixture.detectChanges();
    component.onSave();
    expect(window.alert).toHaveBeenCalledWith("Format d'adresse IP invalide");
  });

  it('should delete a module and emit success', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'deleteModule').and.callThrough();
    spyOn(component.deleteSuccess, 'emit');
    component.onDelete();
    expect(apiService.deleteModule).toHaveBeenCalledWith(mockModule.id);
    expect(component.deleteSuccess.emit).toHaveBeenCalled();
  });

  it('should emit cancel event on cancel', () => {
    spyOn(component.cancel, 'emit');
    component.onCancel();
    expect(component.cancel.emit).toHaveBeenCalled();
  });

  it('should format volume label correctly', () => {
    expect(component.formatVolumeLabel(50)).toBe('50%');
  });
});
