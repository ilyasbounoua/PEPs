/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément, Santiago Alexander RODRIGUEZ TRIANA
 * @description Main application configuration.
 * - provideRouter with withComponentInputBinding: allows route params (:id) to be
 *   bound directly as @Input() on components (used by ModuleDetail).
 * - withRouterConfig paramsInheritanceStrategy: child routes inherit parent params.
 */
import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding, withRouterConfig } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(
      routes,
      withComponentInputBinding(),
      withRouterConfig({ paramsInheritanceStrategy: 'always' })
    ),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch()),
  ],
};
