/**
 * @author Santiago Alexander RODRIGUEZ TRIANA
 * @description Admin guard. Redirects non-admin users to /dashboard.
 * Applied to: /users, /audit-logs, /archive.
 * Must be combined with authGuard (auth is checked first via route ordering).
 */
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

import { toObservable } from '@angular/core/rxjs-interop';
import { filter, firstValueFrom } from 'rxjs';

export const adminGuard: CanActivateFn = async () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    // Wait for initialization (just in case it's called early)
    if (!authService.isInitialized()) {
        await firstValueFrom(
            toObservable(authService.isInitialized).pipe(filter(init => init === true))
        );
    }

    if (authService.isAdmin()) {
        return true;
    }

    return router.createUrlTree(['/dashboard']);
};
