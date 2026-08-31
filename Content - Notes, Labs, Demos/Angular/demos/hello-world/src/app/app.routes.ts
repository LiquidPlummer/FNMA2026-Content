/* ============================================================================
 * THE ROUTE TABLE - the SPA's map of "URL -> which component to render".
 *
 * The Router is what makes an SPA feel like a multi-page site: it watches the
 * address bar, matches the path against this array, and swaps the matched
 * component into the <router-outlet> in the template - all without a page load.
 *
 * It is empty in this demo, which is why app.html hardwires every child
 * component instead. That is also why the build warns NG8113: RouterOutlet is
 * imported by App but never used in its template.
 *
 * A route entry looks like: { path: 'one', component: One }
 * ========================================================================== */

import { Routes } from '@angular/router';

// `routes` is consumed by provideRouter(routes) over in app.config.ts.
export const routes: Routes = [];
