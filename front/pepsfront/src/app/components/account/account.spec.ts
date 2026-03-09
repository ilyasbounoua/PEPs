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


describe('Account', () => {
  let component: Account;
  let fixture: ComponentFixture<Account>;
  // we'll grab the service instance from the same injector used by the component
  let apiService: any;
  let snackBar: MatSnackBar;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Account],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useClass: MockAuthService },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Account);
    component = fixture.componentInstance;
    // obtain the service from the component's injector rather than the root injector
    apiService = fixture.debugElement.injector.get(ApiService) as any;
    // grab the same snackbar instance used by the component itself
    snackBar = (fixture.componentInstance as any).snackBar;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should change password and show snackbar', fakeAsync(() => {
    spyOn(snackBar, 'open');
    spyOn(apiService, 'changePassword').and.callThrough();

    component.currentPassword.set('password');
    component.newPassword.set('newpassword');
    component.confirmPassword.set('newpassword');
    component.submitPasswordChange();

    expect(apiService.changePassword).toHaveBeenCalled();
    // service call has started loading
    expect(component.isLoading()).toBe(true);

    // trigger the same subject instance that the component subscribed to
    apiService.changePassword$.next();
    tick();

    // loading should be turned off after response
    expect(component.isLoading()).toBe(false);
    expect(snackBar.open).toHaveBeenCalledWith('Password changed successfully!', 'OK', { duration: 3000 });
  }));

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
