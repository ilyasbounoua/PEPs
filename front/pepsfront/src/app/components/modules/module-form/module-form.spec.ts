import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ModuleForm } from './module-form';
import { ApiService } from '../../../services/api';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Module } from '../../../models/interfaces';

class MockApiService {
  createModule(module: Omit<Module, 'id'>, role?: string) {
    return of({ id: 1, ...module });
  }
}

describe('ModuleForm', () => {
  let component: ModuleForm;
  let fixture: ComponentFixture<ModuleForm>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleForm],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModuleForm);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create a module and emit success', () => {
    spyOn(apiService, 'createModule').and.callThrough();
    spyOn(component.createSuccess, 'emit');
    component.updateName('Test Module');
    component.updateIp('1.2.3.4');
    component.onCreate();
    expect(apiService.createModule).toHaveBeenCalled();
    expect(component.createSuccess.emit).toHaveBeenCalled();
  });

  it('should show error on create with invalid name', () => {
    component.updateIp('1.2.3.4');
    component.onCreate();
    expect(component.errorMessage()).toBe('Le nom est obligatoire');
  });

  it('should show error on create with invalid IP', () => {
    component.updateName('Test Module');
    component.onCreate();
    expect(component.errorMessage()).toBe("L'adresse IP est obligatoire");
  });

  it('should emit cancel event on cancel', () => {
    spyOn(component.cancel, 'emit');
    component.onCancel();
    expect(component.cancel.emit).toHaveBeenCalled();
  });

  it('should update module properties', () => {
    component.updateName('New Name');
    expect(component.newModule().name).toBe('New Name');

    component.updateIp('5.6.7.8');
    expect(component.newModule().ip).toBe('5.6.7.8');

    component.updateActif(true);
    expect(component.newModule().config.actif).toBe(true);

    component.updateVolume(80);
    expect(component.newModule().config.volume).toBe(80);

    component.updateMode('Automatique');
    expect(component.newModule().config.mode).toBe('Automatique');

    component.updateSon(false);
    expect(component.newModule().config.son).toBe(false);
  });
});
