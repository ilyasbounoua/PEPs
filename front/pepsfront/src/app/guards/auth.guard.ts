/**
 * @author Santiago Alexander RODRIGUEZ TRIANA
 * @description Authentication guard. Redirects unauthenticated users to /login.
 * Applied to all protected routes.
 */
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, firstValueFrom, map, of } from 'rxjs';

/**
 * Authentication guard. Redirects unauthenticated users to /login.
 * 
 * CRITICAL for Lighthouse: It waits for the AuthService to finish its async
 * verification with the backend before deciding to redirect.
 */
export const authGuard: CanActivateFn = async (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    // If not initialized yet, wait for the signal to be true
    if (!authService.isInitialized()) {
        await firstValueFrom(
            toObservable(authService.isInitialized).pipe(filter(init => init === true))
        );
    }

    if (authService.isAuthenticated()) {
        return true;
    }

    // Pass the current requested URL so we can return here after login/verification
    return router.createUrlTree(['/login'], { 
        queryParams: { returnUrl: state.url } 
    });
};
