/**
 * Composant Users
 * Gestion des utilisateurs pour les administrateurs.
 * 
 * Fonctionnalités :
 * - Liste des utilisateurs avec tableau
 * - Création d'un nouvel utilisateur
 * - Modification login/password/role
 * - Suppression d'un utilisateur
 * 
 * Accessible uniquement aux utilisateurs avec role "admin"
 * 
 * @author Anas EL HOUDI
 */
import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../services/api';
import { UserDTO, CreateUserDTO, PermissionType } from '../../models/interfaces';
import { I18nService } from '../../services/i18n';

@Component({
    selector: 'app-users',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatTableModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatDialogModule,
        MatSnackBarModule,
        MatTooltipModule
    ],
    templateUrl: './users.html',
    styleUrl: './users.css'
})
export class Users implements OnInit {

    private api = inject(ApiService);
    private snackBar = inject(MatSnackBar);
    readonly i18n = inject(I18nService);

    // Liste des utilisateurs
    users = signal<UserDTO[]>([]);
    displayedColumns = ['id', 'login', 'role', 'permission', 'enabled', 'actions'];

    // État du formulaire de création/édition
    showForm = signal(false);
    isEditing = signal(false);
    editingUserId = signal<number | null>(null);

    // Formulaire - using regular properties for ngModel binding
    formLogin = '';
    formPassword = '';
    formRole = '';
    formPermission: PermissionType = 'viewer';

    // Available roles loaded from DB
    existingRoles: string[] = [];

    // Role validation error
    roleError = '';

    // Original role when editing (to exclude from validation)
    private originalRole = '';

    ngOnInit(): void {
        this.loadUsers();
        this.loadExistingRoles();
    }

    /**
     * Charge la liste des rôles existants depuis le backend
     */
    loadExistingRoles(): void {
        this.api.getRoles().subscribe({
            next: (roles) => {
                this.existingRoles = roles;
            },
            error: (err) => {
                console.error('Erreur chargement rôles:', err);
            }
        });
    }

    /**
     * Charge la liste des utilisateurs depuis le backend
     */
    loadUsers(): void {
        this.api.getUsers().subscribe({
            next: (users) => {
                // Sort users: Admin (Role) > Editor (Permission) > Viewer (Permission)
                // Note: Permission 'admin' is deprecated.
                const sortedUsers = users.sort((a, b) => {
                    // Helper to get score
                    const getScore = (u: UserDTO) => {
                        if (u.role.toLowerCase() === 'admin') return 3;
                        const p = u.permission.toLowerCase();
                        if (p === 'editor' || p === 'admin') return 2;
                        return 1;
                    };

                    const scoreA = getScore(a);
                    const scoreB = getScore(b);

                    if (scoreA !== scoreB) {
                        return scoreB - scoreA; // Descending score
                    }
                    return a.login.localeCompare(b.login); // Ascending login
                });
                this.users.set(sortedUsers);
            },
            error: (err) => {
                console.error('Erreur chargement utilisateurs:', err);
                this.snackBar.open('Erreur lors du chargement des utilisateurs', 'Fermer', { duration: 3000 });
            }
        });
    }

    /**
     * Affiche le formulaire de création
     */
    openCreateForm(): void {
        this.resetForm();
        this.isEditing.set(false);
        this.showForm.set(true);
    }

    /**
     * Affiche le formulaire d'édition pour un utilisateur
     */
    openEditForm(user: UserDTO): void {
        this.formLogin = user.login;
        this.formPassword = '';
        this.formRole = user.role;
        this.formPermission = ((user.permission as string) === 'admin' ? 'editor' : user.permission) || 'viewer';
        this.originalRole = user.role; // Store original role for validation
        this.roleError = '';
        this.editingUserId.set(user.id);
        this.isEditing.set(true);
        this.showForm.set(true);
    }

    /**
     * Ferme le formulaire
     */
    closeForm(): void {
        this.showForm.set(false);
        this.resetForm();
    }

    /**
     * Réinitialise le formulaire
     */
    private resetForm(): void {
        this.formLogin = '';
        this.formPassword = '';
        this.formRole = '';
        this.formPermission = 'viewer';
        this.roleError = '';
        this.originalRole = '';
        this.editingUserId.set(null);
    }

    /**
     * Valide le rôle entré - BLOQUE les rôles déjà utilisés
     * Chaque rôle ne peut être associé qu'à un seul utilisateur
     */
    validateRole(): boolean {
        if (!this.formRole || this.formRole.trim() === '') {
            this.roleError = this.i18n.t('users.roleRequired');
            return false;
        }

        const roleLower = this.formRole.toLowerCase().trim();

        // Check if role is already used by another user (case-insensitive)
        // Exclude the original role when editing (user can keep their own role)
        const isRoleUsed = this.existingRoles.some(r => {
            const isMatch = r.toLowerCase() === roleLower;
            const isOwnRole = this.isEditing() && this.originalRole.toLowerCase() === roleLower;
            return isMatch && !isOwnRole;
        });

        if (isRoleUsed) {
            this.roleError = `${this.i18n.t('users.roleAlreadyUsed')}: "${this.formRole}"`;
            return false;
        }

        this.roleError = '';
        return true;
    }

    /**
     * Called when role input changes
     */
    onRoleInputChange(): void {
        // Clear error on input change
        if (this.roleError) {
            this.roleError = '';
        }

        // Auto-select 'editor' permission if role is admin
        if (this.formRole && this.formRole.toLowerCase().trim() === 'admin') {
            this.formPermission = 'editor';
        }
    }

    /** True when the entered role is 'admin' — permission select is forced to 'editor'. */
    get isAdminRole(): boolean {
        return this.formRole?.toLowerCase().trim() === 'admin';
    }

    /**
     * Soumet le formulaire (création ou modification)
     */
    submitForm(): void {
        if (!this.validateRole()) {
            return;
        }

        if (this.isEditing()) {
            this.updateUser();
        } else {
            this.createUser();
        }
    }

    /**
     * Helper to get French label for permission
     */
    getPermissionLabel(permission: string): string {
        if (!permission) return '';
        const p = permission.toLowerCase();
        if (p === 'viewer') return this.i18n.t('users.viewer');
        if (p === 'editor') return this.i18n.t('users.editor');
        return permission;
    }

    /**
     * Crée un nouvel utilisateur
     */
    private createUser(): void {
        // Normalize role to lowercase
        const role = this.formRole.toLowerCase().trim();

        const data: CreateUserDTO = {
            login: this.formLogin,
            password: this.formPassword,
            role: role,
            permission: this.formPermission
        };

        this.api.createUser(data).subscribe({
            next: () => {
                this.snackBar.open(this.i18n.t('users.createdSuccess'), this.i18n.t('common.close'), { duration: 3000 });
                this.closeForm();
                this.loadUsers();
            },
            error: (err) => {
                console.error('Erreur création utilisateur:', err);
                const message = err.status === 409 ? this.i18n.t('users.loginExists') : this.i18n.t('users.createError');
                this.snackBar.open(message, this.i18n.t('common.close'), { duration: 3000 });
            }
        });
    }

    /**
     * Modifie un utilisateur existant
     */
    private updateUser(): void {
        const id = this.editingUserId();
        if (!id) return;

        // Normalize role to lowercase
        const role = this.formRole.toLowerCase().trim();

        const data: any = {};
        if (this.formLogin) data.login = this.formLogin;
        if (this.formPassword) data.password = this.formPassword;
        if (role) data.role = role;
        if (this.formPermission) data.permission = this.formPermission;

        this.api.updateUser(id, data).subscribe({
            next: () => {
                this.snackBar.open(this.i18n.t('users.updatedSuccess'), this.i18n.t('common.close'), { duration: 3000 });
                this.closeForm();
                this.loadUsers();
                this.loadExistingRoles(); // Refresh roles list after update
            },
            error: (err) => {
                console.error('Erreur modification utilisateur:', err);
                this.snackBar.open(this.i18n.t('users.updateError'), this.i18n.t('common.close'), { duration: 3000 });
            }
        });
    }

    /**
     * Supprime un utilisateur après confirmation
     */
    deleteUser(user: UserDTO): void {
        if (!confirm(`${this.i18n.t('users.deleteConfirm')} "${user.login}" ?`)) {
            return;
        }

        this.api.deleteUser(user.id).subscribe({
            next: () => {
                this.snackBar.open(this.i18n.t('users.deletedSuccess'), this.i18n.t('common.close'), { duration: 3000 });
                this.loadUsers();
                this.loadExistingRoles(); // Refresh roles list after deletion
            },
            error: (err) => {
                console.error('Erreur suppression utilisateur:', err);
                this.snackBar.open(this.i18n.t('users.deleteError'), this.i18n.t('common.close'), { duration: 3000 });
            }
        });
    }
}
