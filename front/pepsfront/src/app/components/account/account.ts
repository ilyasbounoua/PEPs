/**
 * Profile Component - User profile management
 * Allows the user to change their login and password.
 * 
 * @author Anas EL HOUDI
 */
import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { I18nService } from '../../services/i18n';

@Component({
    selector: 'app-account',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatSnackBarModule,
        MatDividerModule,
    ],
    templateUrl: './account.html',
    styleUrls: ['./account.css'],
})
export class Account {
    private api = inject(ApiService);
    private authService = inject(AuthService);
    private snackBar = inject(MatSnackBar);
    readonly i18n = inject(I18nService);

    // Login change form
    newLogin = signal('');
    isChangingLogin = signal(false);
    loginError = signal('');

    // Password change form
    currentPassword = signal('');
    newPassword = signal('');
    confirmPassword = signal('');

    // States
    isLoading = signal(false);
    showCurrentPassword = signal(false);
    showNewPassword = signal(false);
    showConfirmPassword = signal(false);
    error = signal('');

    // User information
    userLogin = this.authService.currentLogin;
    userRole = this.authService.currentRole;

    /**
     * Submits the login change form.
     */
    submitLoginChange() {
        this.loginError.set('');

        const login = this.newLogin().trim();
        if (!login) {
            this.loginError.set(this.i18n.t('profile.loginRequired'));
            return;
        }
        if (login.length < 3) {
            this.loginError.set(this.i18n.t('profile.loginMinLength'));
            return;
        }

        const userId = this.authService.currentUserId();
        if (!userId) {
            this.loginError.set(this.i18n.t('profile.notLoggedIn'));
            return;
        }

        this.isChangingLogin.set(true);

        this.api.changeLogin(userId, login).subscribe({
            next: () => {
                this.isChangingLogin.set(false);
                this.authService.updateLogin(login);
                this.snackBar.open(this.i18n.t('profile.loginChanged'), 'OK', { duration: 3000 });
                this.newLogin.set('');
            },
            error: (err) => {
                this.isChangingLogin.set(false);
                if (err.status === 409) {
                    this.loginError.set(this.i18n.t('profile.loginTaken'));
                } else {
                    this.loginError.set(this.i18n.t('profile.loginChangeError'));
                }
            }
        });
    }

    /**
     * Submits the password change form.
     */
    submitPasswordChange() {
        this.error.set('');

        // Validation
        if (!this.currentPassword() || !this.newPassword() || !this.confirmPassword()) {
            this.error.set(this.i18n.t('profile.allFieldsRequired'));
            return;
        }

        if (this.newPassword().length < 4) {
            this.error.set(this.i18n.t('profile.passwordMinLength'));
            return;
        }

        if (this.newPassword() !== this.confirmPassword()) {
            this.error.set(this.i18n.t('profile.passwordMismatch'));
            return;
        }

        const userId = this.authService.currentUserId();
        if (!userId) {
            this.error.set(this.i18n.t('profile.notLoggedIn'));
            return;
        }

        this.isLoading.set(true);

        this.api.changePassword(userId, this.currentPassword(), this.newPassword()).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.snackBar.open(this.i18n.t('profile.passwordChanged'), 'OK', { duration: 3000 });
                // Reset form
                this.currentPassword.set('');
                this.newPassword.set('');
                this.confirmPassword.set('');
            },
            error: (err) => {
                this.isLoading.set(false);
                if (err.status === 401) {
                    this.error.set(this.i18n.t('profile.incorrectPassword'));
                } else if (err.error?.error) {
                    this.error.set(err.error.error);
                } else {
                    this.error.set(this.i18n.t('profile.changeError'));
                }
            }
        });
    }

    toggleShowCurrentPassword() {
        this.showCurrentPassword.update(v => !v);
    }

    toggleShowNewPassword() {
        this.showNewPassword.update(v => !v);
    }

    toggleShowConfirmPassword() {
        this.showConfirmPassword.update(v => !v);
    }
}
