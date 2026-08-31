/* ============================================================================
 * DEPENDENCY INJECTION: THE "PROVIDING" SIDE, AT APPLICATION SCOPE.
 *
 * In a standalone project this file replaces the old root AppModule. Where an
 * NgModule used to carry a `providers: []` array for the whole app, a standalone
 * app carries an ApplicationConfig object instead - same job, no module needed.
 *
 * Angular's DI works in two halves, and you need BOTH:
 *   1. PROVIDE  - register a recipe for how to build something (this file).
 *   2. INJECT   - ask for it by type in a constructor or via inject() (app.ts,
 *                 one.ts). If nobody provided it, injection throws NullInjectorError.
 *
 * Everything registered here lands in the ROOT INJECTOR, so it is a single
 * app-wide instance (a singleton) that every component and service can inject.
 * ========================================================================== */

import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { ApiService } from './api-service';

export const appConfig: ApplicationConfig = {
  // This object is handed to bootstrapApplication() in main.ts. It is the
  // configuration of the application's root injector.
  providers: [
    // The `provideXxx()` functions are the modern replacement for importing a
    // library NgModule. Pre-standalone you would have written
    // `imports: [BrowserModule, RouterModule.forRoot(routes)]` in AppModule;
    // now each library exposes a provider function that returns the providers
    // that module used to bundle up.
    provideBrowserGlobalErrorListeners(),

    // Wires up the Router and feeds it our route table from app.routes.ts.
    // This is what makes RouterOutlet / routerLink injectable and functional.
    provideRouter(routes),

    // Providing OUR OWN service the explicit way: the shorthand for
    // { provide: ApiService, useClass: ApiService }. "When somebody injects the
    // ApiService token, new up an ApiService and hand it over - once."
    //
    // Worth noting in class: ApiService is ALSO marked @Injectable({providedIn:'root'}),
    // so this line is redundant. Either mechanism alone gives you one app-wide
    // instance. providedIn:'root' is the preferred style because it lets the
    // bundler tree-shake the service out if nothing ever injects it; listing it
    // here forces it into the bundle whether it is used or not.
    ApiService
  ]
};
