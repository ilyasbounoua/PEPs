/**
 * @author Santiago Alexander RODRIGUEZ TRIANA
 * @description Admin guard. Redirects non-admin users to /dashboard.
 * Applied to: /users, /audit-logs, /archive.
 * Must be combined with authGuard (auth is checked first via route ordering).
 */
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const adminGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isAdmin()) {
        return true;
    }

    return router.createUrlTree(['/dashboard']);
};
