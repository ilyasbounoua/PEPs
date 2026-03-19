/**
 * @author Santiago Alexander RODRIGUEZ TRIANA
 * @description Parametric role guard. Restricts a route to a specific set of roles.
 * Roles in PEPs are dynamic strings (admin, aras, dauphin, or any future role created by admin).
 *
 * Usage in routes:
 *   canActivate: [authGuard, roleGuard(['dauphin', 'aras'])]
 */
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
    return () => {
        const authService = inject(AuthService);
        const router = inject(Router);

        const currentRole = authService.currentRole();

        if (allowedRoles.includes(currentRole)) {
            return true;
        }

        return router.createUrlTree(['/dashboard']);
    };
};
