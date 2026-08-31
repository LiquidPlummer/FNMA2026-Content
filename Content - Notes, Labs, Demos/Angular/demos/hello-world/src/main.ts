/* ============================================================================
 * BOOTSTRAPPING THE SPA  -  this file is the entry point of the whole app.
 *
 * Angular apps are Single Page Applications (SPAs): the browser loads ONE html
 * page (index.html) exactly once, and from then on Angular swaps DOM in and out
 * with JavaScript instead of asking the server for new pages.
 *
 * "Bundling" is what the Angular CLI (`ng serve` / `ng build`) does before any
 * of this runs: it compiles our TypeScript to JavaScript, compiles our html
 * templates into render instructions, tree-shakes out anything unreachable, and
 * emits a handful of .js bundles into dist/. index.html gets <script> tags for
 * those bundles injected automatically. The FIRST bundle to execute runs THIS
 * file, so main.ts is the seam between "the browser loaded some JS" and
 * "an Angular app is now running".
 * ========================================================================== */

// bootstrapApplication is the STANDALONE way to start an app (Angular 14+).
// The old, pre-standalone way was platformBrowserDynamic().bootstrapModule(AppModule),
// which needed a root NgModule (usually called AppModule) to exist. We don't have
// one - this is a standalone project - so we hand Angular a root COMPONENT instead.
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// Two arguments, and they map onto two big ideas:
//   App        -> the ROOT COMPONENT. Angular finds the element matching its
//                 selector ('app-root') inside index.html and renders there.
//                 Every other component in the app hangs off this one, forming
//                 the component tree.
//   appConfig  -> the ROOT INJECTOR's configuration: the app-wide list of
//                 PROVIDERS (see app.config.ts). This is where dependency
//                 injection for the whole application gets set up.
//
// bootstrapApplication returns a Promise, so .catch() is how we see errors that
// happen during startup instead of losing them silently.
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
