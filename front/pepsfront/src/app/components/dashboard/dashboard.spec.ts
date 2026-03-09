import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Dashboard } from './dashboard';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

class MockApiService {
  getDashboardStats() {
    return of({ totalInteractions: 100, activeModules: 10, lastInteraction: '2024-01-01' });
  }
  getDailyStats() {
    return of([{ time: '2024-01-01', count: 10 }]);
  }
  getRoles() {
    return of(['dauphin', 'aras']);
  }
}

describe('Dashboard as a regular user', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useValue: { isAdmin: () => false } },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load data on init', () => {
    spyOn(apiService, 'getDashboardStats').and.callThrough();
    spyOn(apiService, 'getDailyStats').and.callThrough();
    component.ngOnInit();
    expect(apiService.getDashboardStats).toHaveBeenCalled();
    expect(apiService.getDailyStats).toHaveBeenCalled();
    expect(component.stats().totalInteractions).toBe(100);
  });

  it('should not load roles on init', () => {
    spyOn(apiService, 'getRoles').and.callThrough();
    component.ngOnInit();
    expect(apiService.getRoles).not.toHaveBeenCalled();
  });

  it('should change period and reload data', () => {
    spyOn(component, 'loadData').and.callThrough();
    component.onPeriodChange('week');
    expect(component.selectedPeriod).toBe('week');
    expect(component.loadData).toHaveBeenCalled();
  });
});

describe('Dashboard as an admin', () => {
    let component: Dashboard;
    let fixture: ComponentFixture<Dashboard>;
    let apiService: ApiService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
        imports: [Dashboard],
        providers: [
            { provide: ApiService, useClass: MockApiService },
            { provide: AuthService, useValue: { isAdmin: () => true } },
            provideNoopAnimations(),
        ],
        }).compileComponents();

        fixture = TestBed.createComponent(Dashboard);
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

    it('should change profile and reload data', () => {
        spyOn(component, 'loadData').and.callThrough();
        component.onProfileChange('aras');
        expect(component.selectedRole).toBe('aras');
        expect(component.loadData).toHaveBeenCalled();
    });
});

describe('Dashboard - Common', () => {
    let component: Dashboard;
    let fixture: ComponentFixture<Dashboard>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [Dashboard],
            providers: [
                { provide: ApiService, useClass: MockApiService },
                { provide: AuthService, useValue: { isAdmin: () => false } },
                provideNoopAnimations(),
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(Dashboard);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should format labels correctly', () => {
        component.selectedPeriod = 'today';
        expect(component.formatLabel('8h')).toBe('8h');
        component.selectedPeriod = 'week';
        expect(component.formatLabel('2024-10-25')).toBe('25/10');
    });

    it('should handle pagination', () => {
        const dailyData = Array.from({ length: 20 }, (_, i) => ({ time: `2024-01-${i + 1}`, count: i }));
        component.dailyChartData.set(dailyData);
        component.selectedPeriod = 'month'; // 7 days per page
        
        expect(component.paginatedChartData.length).toBe(7);
        expect(component.hasPreviousPage()).toBe(true);
        expect(component.hasNextPage()).toBe(false);

        component.previousPage();
        expect(component.visibleWeekIndex).toBe(1);
        expect(component.hasNextPage()).toBe(true);

        component.nextPage();
        expect(component.visibleWeekIndex).toBe(0);
    });

    it('should get correct range label', () => {
        component.dailyChartData.set([{ time: '2024-01-01', count: 10 }, { time: '2024-01-07', count: 20 }]);
        component.selectedPeriod = 'week';
        expect(component.getRangeLabel()).toBe('Du 01/01 au 07/01');

        component.dailyChartData.set([{ time: '8h', count: 10 }, { time: '10h', count: 20 }]);
        component.selectedPeriod = 'today';
        expect(component.getRangeLabel()).toBe('8h - 10h');
    });
});
