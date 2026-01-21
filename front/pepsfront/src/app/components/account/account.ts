/**
 * Account Component - User account management
 * Allows the user to change their password.
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
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';

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
    ],
    templateUrl: './account.html',
    styleUrls: ['./account.css'],
})
export class Account {
    private api = inject(ApiService);
    private authService = inject(AuthService);
    private snackBar = inject(MatSnackBar);

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
     * Submits the password change form.
     */
    submitPasswordChange() {
        this.error.set('');

        // Validation
        if (!this.currentPassword() || !this.newPassword() || !this.confirmPassword()) {
            this.error.set('All fields are required');
            return;
        }

        if (this.newPassword().length < 4) {
            this.error.set('New password must be at least 4 characters');
            return;
        }

        if (this.newPassword() !== this.confirmPassword()) {
            this.error.set('New passwords do not match');
            return;
        }

        const userId = this.authService.currentUserId();
        if (!userId) {
            this.error.set('User not logged in');
            return;
        }

        this.isLoading.set(true);

        this.api.changePassword(userId, this.currentPassword(), this.newPassword()).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.snackBar.open('Password changed successfully!', 'OK', { duration: 3000 });
                // Reset form
                this.currentPassword.set('');
                this.newPassword.set('');
                this.confirmPassword.set('');
            },
            error: (err) => {
                this.isLoading.set(false);
                if (err.status === 401) {
                    this.error.set('Current password is incorrect');
                } else if (err.error?.error) {
                    this.error.set(err.error.error);
                } else {
                    this.error.set('Error changing password');
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
