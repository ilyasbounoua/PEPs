/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément, Santiago Alexander RODRIGUEZ TRIANA
 * @description Server-side rendering route configuration.
 *
 * RenderMode.Prerender  — page is pre-rendered at build time (static, no dynamic params).
 * RenderMode.Server     — page is rendered on the server per request (required for routes
 *                         with dynamic parameters like :id, which are unknown at build time).
 *
 * The `modules/:id` route MUST use RenderMode.Server because the module ID is not
 * known at build time (it comes from the database).
 */
import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Dynamic routes — rendered on-demand per request
  {
    path: 'modules/:id',
    renderMode: RenderMode.Server,
  },

  // All other routes — pre-rendered at build time
  {
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
