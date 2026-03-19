import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AuditLogsComponent } from './audit-logs';
import { ApiService } from '../../services/api';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { AuditLog } from '../../models/interfaces';

class MockApiService {
  getAuditLogs() {
    const logs: AuditLog[] = [
      { id: 1, action: 'CREATE', entityType: 'module', entityId: 1, entityName: 'Module 1', entityRole: 'admin', userLogin: 'admin', timestamp: new Date().toISOString(), oldValue: null, newValue: '{}', details: '' },
      { id: 2, action: 'UPDATE', entityType: 'sound', entityId: 2, entityName: 'Sound 2', entityRole: 'user', userLogin: 'user', timestamp: new Date().toISOString(), oldValue: '{}', newValue: '{"name":"new name"}', details: '' },
    ];
    return of(logs);
  }
  getAuditLogsByEntity(entity: string) {
    const logs: AuditLog[] = [
      { id: 1, action: 'CREATE', entityType: 'module', entityId: 1, entityName: 'Module 1', entityRole: 'admin', userLogin: 'admin', timestamp: new Date().toISOString(), oldValue: null, newValue: '{}', details: '' },
    ];
    return of(logs);
  }
}

describe('AuditLogsComponent', () => {
  let component: AuditLogsComponent;
  let fixture: ComponentFixture<AuditLogsComponent>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditLogsComponent],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AuditLogsComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load logs on init', () => {
    spyOn(apiService, 'getAuditLogs').and.callThrough();
    component.ngOnInit();
    expect(apiService.getAuditLogs).toHaveBeenCalled();
    expect(component.logs().length).toBe(2);
  });

  it('should filter logs by entity', () => {
    spyOn(apiService, 'getAuditLogsByEntity').and.callThrough();
    component.filterEntity.set('module');
    component.onFilterChange();
    expect(apiService.getAuditLogsByEntity).toHaveBeenCalledWith('module');
    expect(component.logs().length).toBe(1);
  });

  it('should toggle expand log', () => {
    expect(component.isExpanded(1)).toBe(false);
    component.toggleExpand(1);
    expect(component.isExpanded(1)).toBe(true);
    component.toggleExpand(1);
    expect(component.isExpanded(1)).toBe(false);
  });

  it('should get correct action icon and class', () => {
    expect(component.getActionIcon('CREATE')).toBe('add_circle');
    expect(component.getActionClass('CREATE')).toBe('action-create');
    expect(component.getActionIcon('UPDATE')).toBe('edit');
    expect(component.getActionClass('UPDATE')).toBe('action-update');
    expect(component.getActionIcon('DELETE')).toBe('delete');
    expect(component.getActionClass('DELETE')).toBe('action-delete');
  });

  it('should format entity info', () => {
    const log: AuditLog = { id: 1, action: 'CREATE', entityType: 'module', entityId: 1, entityName: 'Module 1', entityRole: 'admin', userLogin: 'admin', timestamp: new Date().toISOString(), oldValue: null, newValue: '{}', details: '' };
    expect(component.formatEntityInfo(log)).toBe('module "Module 1" #1 (admin)');
  });
});
