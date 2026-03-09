import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Users } from './users';
import { ApiService } from '../../services/api';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { UserDTO } from '../../models/interfaces';

class MockApiService {
  getUsers() {
    const users: UserDTO[] = [
      { id: 1, login: 'user1', role: 'dauphin', permission: 'viewer', enabled: true },
      { id: 2, login: 'user2', role: 'aras', permission: 'editor', enabled: false },
      { id: 3, login: 'admin', role: 'admin', permission: 'editor', enabled: true },
    ];
    return of(users);
  }
  getRoles() {
    return of(['dauphin', 'aras']);
  }
  createUser(data: any) {
    return of({ id: 4, ...data });
  }
  updateUser(id: number, data: any) {
    return of({ id, ...data });
  }
  deleteUser(id: number) {
    return of(undefined);
  }
}

class MockSnackBar {
  open(message: string, action: string, config: any) {}
}

describe('Users', () => {
  let component: Users;
  let fixture: ComponentFixture<Users>;
  let apiService: ApiService;
  let snackBar: MatSnackBar;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Users],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: MatSnackBar, useClass: MockSnackBar },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Users);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    snackBar = TestBed.inject(MatSnackBar);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load users and roles on init', () => {
    spyOn(apiService, 'getUsers').and.callThrough();
    spyOn(apiService, 'getRoles').and.callThrough();
    component.ngOnInit();
    expect(apiService.getUsers).toHaveBeenCalled();
    expect(apiService.getRoles).toHaveBeenCalled();
    expect(component.users().length).toBe(3);
    expect(component.existingRoles.length).toBe(2);
  });

  it('should open and close create form', () => {
    component.openCreateForm();
    expect(component.showForm()).toBe(true);
    expect(component.isEditing()).toBe(false);
    component.closeForm();
    expect(component.showForm()).toBe(false);
  });

  it('should open and close edit form', () => {
    const user = component.users()[0];
    component.openEditForm(user);
    expect(component.showForm()).toBe(true);
    expect(component.isEditing()).toBe(true);
    expect(component.editingUserId()).toBe(user.id);
    component.closeForm();
    expect(component.showForm()).toBe(false);
  });

  it('should validate role', () => {
    component.formRole = 'newrole';
    expect(component.validateRole()).toBe(true);
    component.formRole = 'dauphin';
    expect(component.validateRole()).toBe(false);
  });

  it('should create user on submit', () => {
    spyOn(apiService, 'createUser').and.callThrough();
    component.openCreateForm();
    component.formLogin = 'newuser';
    component.formPassword = 'password';
    component.formRole = 'newrole';
    component.submitForm();
    expect(apiService.createUser).toHaveBeenCalled();
  });

  it('should update user on submit', () => {
    spyOn(apiService, 'updateUser').and.callThrough();
    const user = component.users()[0];
    component.openEditForm(user);
    component.formLogin = 'updateduser';
    component.submitForm();
    expect(apiService.updateUser).toHaveBeenCalled();
  });

  it('should delete user', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'deleteUser').and.callThrough();
    const user = component.users()[0];
    component.deleteUser(user);
    expect(apiService.deleteUser).toHaveBeenCalledWith(user.id);
  });
});
