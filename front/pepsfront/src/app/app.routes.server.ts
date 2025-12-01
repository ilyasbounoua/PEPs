/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the server-specific route configuration, which defines the rendering mode for all routes.
 */
import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
