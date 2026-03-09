import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ModuleForm } from './module-form';
import { ApiService } from '../../../services/api';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Module } from '../../../models/interfaces';
import { Router, ActivatedRoute } from '@angular/router';

class MockApiService {
  createModule(module: Omit<Module, 'id'>, role?: string) {
    return of({ id: 1, ...module });
  }
}

describe('ModuleForm', () => {
  let component: ModuleForm;
  let fixture: ComponentFixture<ModuleForm>;
  let apiService: ApiService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModuleForm],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => null,
              },
            },
          },
        },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModuleForm);
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

  it('should create a module and navigate', () => {
    spyOn(apiService, 'createModule').and.callThrough();
    component.updateName('Test Module');
    component.updateIp('1.2.3.4');
    component.onCreate();
    expect(apiService.createModule).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/modules']);
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

  it('should navigate on cancel', () => {
    component.onCancel();
    expect(router.navigate).toHaveBeenCalledWith(['/modules']);
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
