import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ArchiveComponent } from './archive';
import { ApiService } from '../../services/api';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ArchivePeriod, AuditArchivePeriod } from '../../models/interfaces';

class MockApiService {
  getArchivePeriods() {
    const periods: ArchivePeriod[] = [
      { periodId: '2024-Q1', periodLabel: 'Jan-Mar 2024', startDate: '', endDate: '', interactionCount: 100 },
    ];
    return of(periods);
  }
  getAuditArchivePeriods() {
    const periods: AuditArchivePeriod[] = [
      { periodId: '2024-Q1', periodLabel: 'Jan-Mar 2024', startDate: '', endDate: '', interactionCount: 50 },
    ];
    return of(periods);
  }
  exportAndDeletePeriod(periodId: string) {
    return of(new Blob());
  }
  exportAndDeleteAllPeriods() {
    return of(new Blob());
  }
  exportAndDeleteAuditPeriod(periodId: string) {
    return of(new Blob());
  }
  exportAndDeleteAllAuditPeriods() {
    return of(new Blob());
  }
}

describe('ArchiveComponent', () => {
  let component: ArchiveComponent;
  let fixture: ComponentFixture<ArchiveComponent>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArchiveComponent],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArchiveComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load periods on init', () => {
    spyOn(apiService, 'getArchivePeriods').and.callThrough();
    spyOn(apiService, 'getAuditArchivePeriods').and.callThrough();
    component.ngOnInit();
    expect(apiService.getArchivePeriods).toHaveBeenCalled();
    expect(apiService.getAuditArchivePeriods).toHaveBeenCalled();
    expect(component.periods().length).toBe(1);
    expect(component.auditPeriods().length).toBe(1);
  });

  it('should export a period', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'exportAndDeletePeriod').and.callThrough();
    const period = component.periods()[0];
    component.exportPeriod(period);
    expect(apiService.exportAndDeletePeriod).toHaveBeenCalledWith(period.periodId);
  });

  it('should export all periods', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'exportAndDeleteAllPeriods').and.callThrough();
    component.exportAll();
    expect(apiService.exportAndDeleteAllPeriods).toHaveBeenCalled();
  });

  it('should export an audit period', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'exportAndDeleteAuditPeriod').and.callThrough();
    const period = component.auditPeriods()[0];
    component.exportAuditPeriod(period);
    expect(apiService.exportAndDeleteAuditPeriod).toHaveBeenCalledWith(period.periodId);
  });

  it('should export all audit periods', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'exportAndDeleteAllAuditPeriods').and.callThrough();
    component.exportAllAudit();
    expect(apiService.exportAndDeleteAllAuditPeriods).toHaveBeenCalled();
  });
});
