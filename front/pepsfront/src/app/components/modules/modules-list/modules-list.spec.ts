import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ModulesList } from './modules-list';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Module } from '../../../models/interfaces';
import { Router } from '@angular/router';

class MockApiService {
  getModules(role?: string) {
    const modules: Module[] = [
      { id: 1, name: 'Module 1', location: 'Location 1', status: 'Actif', ip: '1.1.1.1', config: { volume: 50, mode: 'Manuel', actif: true, son: true } },
      { id: 2, name: 'Module 2', location: 'Location 2', status: 'Inactif', ip: '2.2.2.2', config: { volume: 50, mode: 'Manuel', actif: true, son: true } },
    ];
    return of(modules);
  }
  getRoles() {
    return of(['dauphin', 'aras']);
  }
}

describe('ModulesList as a regular user', () => {
  let component: ModulesList;
  let fixture: ComponentFixture<ModulesList>;
  let apiService: ApiService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModulesList],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useValue: { isAdmin: () => false, canEdit: () => false } },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModulesList);
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

  it('should load modules on init', () => {
    spyOn(apiService, 'getModules').and.callThrough();
    component.ngOnInit();
    expect(apiService.getModules).toHaveBeenCalled();
    expect(component.modules().length).toBe(2);
  });

  it('should not load roles on init', () => {
    spyOn(apiService, 'getRoles').and.callThrough();
    component.ngOnInit();
    expect(apiService.getRoles).not.toHaveBeenCalled();
  });

  it('should navigate on module click', () => {
    const module = component.modules()[0];
    component.onModuleClick(module);
    expect(router.navigate).toHaveBeenCalledWith(['/modules', module.id]);
  });
});

describe('ModulesList as an admin', () => {
    let component: ModulesList;
    let fixture: ComponentFixture<ModulesList>;
    let apiService: ApiService;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
        imports: [ModulesList],
        providers: [
            { provide: ApiService, useClass: MockApiService },
            { provide: AuthService, useValue: { isAdmin: () => true, canEdit: () => true } },
            provideNoopAnimations(),
        ],
        }).compileComponents();

        fixture = TestBed.createComponent(ModulesList);
        component = fixture.componentInstance;
        apiService = TestBed.inject(ApiService);
        router = TestBed.inject(Router);
        // Jasmine spy for router navigation
        spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
        fixture.detectChanges();
    });

    it('should load roles on init', () => {
        spyOn(apiService, 'getRoles').and.callThrough();
        component.ngOnInit();
        expect(apiService.getRoles).toHaveBeenCalled();
        expect(component.profiles.length).toBe(3);
    });

    it('should reload data on profile change', () => {
        spyOn(component, 'loadData').and.callThrough();
        component.onProfileChange('aras');
        expect(component.selectedRole).toBe('aras');
        expect(component.loadData).toHaveBeenCalled();
    });

    it('should navigate on add click', () => {
        component.onAddClick();
        expect(router.navigate).toHaveBeenCalledWith(['/modules/new'], {});
    });

    it('should navigate on add click with role', () => {
        component.selectedRole = 'aras';
        component.onAddClick();
        expect(router.navigate).toHaveBeenCalledWith(['/modules/new'], { queryParams: { role: 'aras' } });
    });
});
