/* ============================================================================
 * A STANDALONE CHILD COMPONENT.
 *
 * Nothing special marks this as "standalone" - since Angular 19 that is simply
 * the default. A standalone component is self-contained: it names its own
 * template dependencies in `imports` and can be used anywhere by importing the
 * class. No NgModule has to know it exists.
 * ========================================================================== */

import { Component, inject } from '@angular/core';
import { OneChild } from './one-child/one-child';
import { ApiService } from '../api-service';

@Component({
  // App can render <app-one> because App listed this class in ITS imports array.
  selector: 'app-one',

  // ...and this component can render <app-one-child> in one.html because it
  // lists OneChild here. Every standalone component has its own compilation
  // scope - imports are NOT inherited from the parent. App importing OneChild
  // would do nothing for this template; each component asks for its own.
  imports: [OneChild],//providing!

  templateUrl: './one.html',
  styleUrl: './one.css',
})
export class One {
  // INJECTING the same singleton App injected. Both components hold a reference
  // to one shared ApiService instance - that is how a service becomes a
  // communication channel between components that have no parent/child link.
  apiService: ApiService = inject(ApiService);

  //constructor injecting - we need the thing to be "provided" and then we can "inject"
  // The comment above is the rule in one line: PROVIDE first (in app.config.ts
  // or via @Injectable({providedIn:'root'})), then you may INJECT. Injecting
  // something nobody provided throws NullInjectorError at runtime.
  constructor(apiService: ApiService) {
    this.apiService = apiService
  }

}
