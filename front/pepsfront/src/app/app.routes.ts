/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément, Santiago Alexander RODRIGUEZ TRIANA
 * @description Main application routes.
 *
 * Route access levels:
 * - Public:     /login
 * - Protected:  all other routes (authGuard)
 * - Admin only: /users, /audit-logs, /archive (authGuard + adminGuard)
 *
 * Data filtering by role (aras, dauphin, etc.) is handled at the API level,
 * not at the route level — all authenticated non-admin users see the same routes.
 */
import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
    // Public
    {
        path: 'login',
        loadComponent: () => import('./components/login/login').then(m => m.Login),
    },

    // Protected — authenticated users (any role)
    {
        path: 'dashboard',
        canActivate: [authGuard],
        loadComponent: () => import('./components/dashboard/dashboard').then(m => m.Dashboard),
    },
    {
        path: 'interactions',
        canActivate: [authGuard],
        loadComponent: () => import('./components/interactions/interactions').then(m => m.Interactions),
    },
    {
        path: 'modules',
        canActivate: [authGuard],
        loadComponent: () => import('./components/modules/modules-list/modules-list').then(m => m.ModulesList),
    },
    {
        path: 'modules/new',
        canActivate: [authGuard],
        loadComponent: () => import('./components/modules/module-form/module-form').then(m => m.ModuleForm),
    },
    {
        path: 'modules/:id',
        canActivate: [authGuard],
        loadComponent: () => import('./components/modules/module-detail/module-detail').then(m => m.ModuleDetail),
    },
    {
        path: 'sounds',
        canActivate: [authGuard],
        loadComponent: () => import('./components/sounds/sounds').then(m => m.Sounds),
    },
    {
        path: 'account',
        canActivate: [authGuard],
        loadComponent: () => import('./components/account/account').then(m => m.Account),
    },

    // Admin only
    {
        path: 'users',
        canActivate: [authGuard, adminGuard],
        loadComponent: () => import('./components/users/users').then(m => m.Users),
    },
    {
        path: 'audit-logs',
        canActivate: [authGuard, adminGuard],
        loadComponent: () => import('./components/audit-logs/audit-logs').then(m => m.AuditLogsComponent),
    },
    {
        path: 'archive',
        canActivate: [authGuard, adminGuard],
        loadComponent: () => import('./components/archive/archive').then(m => m.ArchiveComponent),
    },

    // Redirects
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: '**', redirectTo: 'dashboard' },
];
