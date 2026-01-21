/**
 * Composant Account - Gestion du compte utilisateur
 * Permet à l'utilisateur de modifier son mot de passe.
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

    // Formulaire de changement de mot de passe
    currentPassword = signal('');
    newPassword = signal('');
    confirmPassword = signal('');

    // États
    isLoading = signal(false);
    showCurrentPassword = signal(false);
    showNewPassword = signal(false);
    showConfirmPassword = signal(false);
    error = signal('');

    // Informations utilisateur
    userLogin = this.authService.currentLogin;
    userRole = this.authService.currentRole;

    /**
     * Soumet le formulaire de changement de mot de passe.
     */
    submitPasswordChange() {
        this.error.set('');

        // Validation
        if (!this.currentPassword() || !this.newPassword() || !this.confirmPassword()) {
            this.error.set('Tous les champs sont obligatoires');
            return;
        }

        if (this.newPassword().length < 4) {
            this.error.set('Le nouveau mot de passe doit contenir au moins 4 caractères');
            return;
        }

        if (this.newPassword() !== this.confirmPassword()) {
            this.error.set('Les nouveaux mots de passe ne correspondent pas');
            return;
        }

        const userId = this.authService.currentUserId();
        if (!userId) {
            this.error.set('Utilisateur non connecté');
            return;
        }

        this.isLoading.set(true);

        this.api.changePassword(userId, this.currentPassword(), this.newPassword()).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.snackBar.open('Mot de passe modifié avec succès !', 'OK', { duration: 3000 });
                // Réinitialiser le formulaire
                this.currentPassword.set('');
                this.newPassword.set('');
                this.confirmPassword.set('');
            },
            error: (err) => {
                this.isLoading.set(false);
                if (err.status === 401) {
                    this.error.set('Mot de passe actuel incorrect');
                } else if (err.error?.error) {
                    this.error.set(err.error.error);
                } else {
                    this.error.set('Erreur lors du changement de mot de passe');
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
