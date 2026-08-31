/* ============================================================================
 * A SERVICE - the "S" half of the components-and-services split.
 *
 * Components own a piece of the UI. Services own everything that is NOT UI:
 * shared state, http calls, business logic. Keeping them separate is what lets
 * many components share one source of truth (see how App and One both inject
 * this exact same object).
 * ========================================================================== */

import { Injectable } from '@angular/core';

/* ---------------------------------------------------------------------------
 * DECORATOR: @Injectable
 *
 * A decorator is a TypeScript feature - a function, prefixed with @, applied to
 * a class - that attaches METADATA to that class at compile time. The class
 * below is a plain TypeScript class; the decorator is what makes Angular aware
 * of it. Angular's compiler reads this metadata and generates extra static
 * members on the class describing how to construct it and what it depends on.
 *
 * @Injectable specifically means "this class participates in dependency
 * injection." Two things follow from it:
 *   - Angular is allowed to CREATE it for you, resolving its own constructor
 *     dependencies recursively.
 *   - The class itself becomes the DI TOKEN - the key other classes ask for.
 * ------------------------------------------------------------------------- */
@Injectable({//PROVIDING
  // providedIn: 'root' is the PROVIDING half of DI, declared right here on the
  // service instead of in a providers array. It means: register this in the
  // application's root injector, and make it a SINGLETON - one instance shared
  // by the entire app, created lazily the first time somebody injects it.
  //
  // This is the modern default and it is tree-shakable: if no code ever injects
  // ApiService, the bundler can drop it from the bundle entirely.
  // (The old way was listing the class in an NgModule's providers array, or in
  // appConfig.providers - which app.config.ts also does here, redundantly.)
  providedIn: 'root'
})
export class ApiService {

  // Because this is a singleton, these fields are effectively app-wide shared
  // state. If One mutates count, App sees the change - same object.
  dataState: String = "Hello"
  count: number = 0;

  testFunc() {
    console.log("The service is active")
  }
}
