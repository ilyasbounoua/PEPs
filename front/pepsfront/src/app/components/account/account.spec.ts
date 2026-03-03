import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { Account } from './account';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

class MockApiService {
  changePassword$ = new Subject<void>();
  changePassword(userId: number, currentPassword: string, newPassword: string) {
    return this.changePassword$.asObservable();
  }
}

class MockAuthService {
  currentUserId = () => 1;
  currentLogin = () => 'testuser';
  currentRole = () => 'admin';
}

class MockSnackBar {
  open(message: string, action: string, config: any) {}
}

describe('Account', () => {
  let component: Account;
  let fixture: ComponentFixture<Account>;
  let apiService: any;
  let snackBar: MatSnackBar;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Account],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useClass: MockAuthService },
        { provide: MatSnackBar, useClass: MockSnackBar },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Account);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    snackBar = TestBed.inject(MatSnackBar);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should change password and show snackbar', () => {
    spyOn(snackBar, 'open');
    component.currentPassword.set('password');
    component.newPassword.set('newpassword');
    component.confirmPassword.set('newpassword');
    component.submitPasswordChange();
    
    apiService.changePassword$.next();

    expect(snackBar.open).toHaveBeenCalledWith('Password changed successfully!', 'OK', { duration: 3000 });
  });

  it('should show error if fields are empty', () => {
    component.submitPasswordChange();
    expect(component.error()).toBe('All fields are required');
  });

  it('should show error if new password is too short', () => {
    component.currentPassword.set('password');
    component.newPassword.set('123');
    component.confirmPassword.set('123');
    component.submitPasswordChange();
    expect(component.error()).toBe('New password must be at least 4 characters');
  });

  it('should show error if new passwords do not match', () => {
    component.currentPassword.set('password');
    component.newPassword.set('newpassword');
    component.confirmPassword.set('wrongpassword');
    component.submitPasswordChange();
    expect(component.error()).toBe('New passwords do not match');
  });

  it('should toggle password visibility', () => {
    expect(component.showCurrentPassword()).toBe(false);
    component.toggleShowCurrentPassword();
    expect(component.showCurrentPassword()).toBe(true);

    expect(component.showNewPassword()).toBe(false);
    component.toggleShowNewPassword();
    expect(component.showNewPassword()).toBe(true);

    expect(component.showConfirmPassword()).toBe(false);
    component.toggleShowConfirmPassword();
    expect(component.showConfirmPassword()).toBe(true);
  });
});
