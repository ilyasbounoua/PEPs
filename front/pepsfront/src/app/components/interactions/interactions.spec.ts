import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Interactions } from './interactions';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Interaction } from '../../models/interfaces';

class MockApiService {
  getInteractions(role?: string) {
    const interactions: Interaction[] = [
      { id: 1, date: new Date().toISOString(), module: 'Module A', type: 'Type A' },
      { id: 2, date: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), module: 'Module B', type: 'Type B' },
      { id: 3, date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(), module: 'Module C', type: 'Type C' },
    ];
    return of(interactions);
  }
  getRoles() {
    return of(['dauphin', 'aras']);
  }
}

describe('Interactions as a regular user', () => {
  let component: Interactions;
  let fixture: ComponentFixture<Interactions>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Interactions],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useValue: { isAdmin: () => false } },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Interactions);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load interactions on init', () => {
    spyOn(apiService, 'getInteractions').and.callThrough();
    component.ngOnInit();
    expect(apiService.getInteractions).toHaveBeenCalled();
    expect(component.interactions().length).toBe(3);
  });

  it('should not load roles on init', () => {
    spyOn(apiService, 'getRoles').and.callThrough();
    component.ngOnInit();
    expect(apiService.getRoles).not.toHaveBeenCalled();
  });

  it('should filter interactions by "today"', () => {
    component.setFilter('today');
    expect(component.filteredInteractions().length).toBe(1);
  });

  it('should filter interactions by "yesterday"', () => {
    component.setFilter('yesterday');
    expect(component.filteredInteractions().length).toBe(1);
  });

  it('should filter interactions by "week"', () => {
    component.setFilter('week');
    expect(component.filteredInteractions().length).toBe(3);
  });

  it('should export data as CSV', () => {
    spyOn(URL, 'createObjectURL').and.returnValue('csv-url');
    const link = { setAttribute: () => {}, click: () => {}, remove: () => {} };
    spyOn(document, 'createElement').and.returnValue(link as any);
    spyOn(document.body, 'appendChild');
    spyOn(document.body, 'removeChild');

    component.exportAsCsv();

    expect(document.createElement).toHaveBeenCalledWith('a');
    expect(document.body.appendChild).toHaveBeenCalled();
    expect(document.body.removeChild).toHaveBeenCalled();
  });
});

describe('Interactions as an admin', () => {
    let component: Interactions;
    let fixture: ComponentFixture<Interactions>;
    let apiService: ApiService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
        imports: [Interactions],
        providers: [
            { provide: ApiService, useClass: MockApiService },
            { provide: AuthService, useValue: { isAdmin: () => true } },
            provideNoopAnimations(),
        ],
        }).compileComponents();

        fixture = TestBed.createComponent(Interactions);
        component = fixture.componentInstance;
        apiService = TestBed.inject(ApiService);
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
});
