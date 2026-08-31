/* ============================================================================
 * THE ROOT COMPONENT.
 *
 * main.ts bootstraps this class, index.html has its <app-root> tag, and every
 * other component in the app is reached from its template. This is the top of
 * the COMPONENT TREE.
 * ========================================================================== */

import { Component, inject, signal } from '@angular/core';
import { One } from './one/one';
import { Two } from './two/two';
import { ApiService } from './api-service';

/* ---------------------------------------------------------------------------
 * DECORATOR: @Component
 *
 * Same TypeScript mechanism as @Injectable - a function applied to a class that
 * records metadata about it. @Component says "this class is a UI building
 * block", and the object below is the recipe Angular's compiler uses to turn
 * the class + its html file into a custom element it knows how to render.
 *
 * The decorator is not decoration. Delete it and this becomes an ordinary
 * TypeScript class that Angular cannot render, cannot inject into, and cannot
 * find by tag name.
 *
 * A COMPONENT = decorator metadata + a template (html) + a class (behaviour and
 * state) + optional styles. Those four things together are the unit you reuse.
 * ------------------------------------------------------------------------- */
@Component({
  // The custom tag name this component answers to. Angular scans templates for
  // this string. It is why <app-root> in index.html renders this class.
  selector: 'app-root',

  // ==== STANDALONE COMPONENTS + THE "IMPORTING" HALF OF DI ================
  // Since Angular 19 every component is STANDALONE by default - it declares its
  // own dependencies right here rather than being declared inside an NgModule.
  // That is what makes this a "standalone project": there is no AppModule.
  //
  // This imports array is the component's own private compilation scope. A
  // template can only use selectors, directives and pipes that appear in this
  // list. It is the reason app.html can write <app-one> and <app-mod-one>:
  // those classes are listed here. Anything not listed is an unknown element
  // (compiler error NG8001) - exactly what mod-one/two/three currently hit with
  // <app-outside-component>.
  //
  // Note this is a DIFFERENT kind of "providing" than a service provider: this
  // makes TEMPLATE symbols visible, it does not create injectable instances.
  imports: [One, Two],

  // Template and styles could be inline (template: / styles:), but the CLI
  // scaffolds them as separate files. Either way the compiler inlines them into
  // the bundle at build time - the .html file is not fetched at runtime.
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  // SIGNALS - Angular's reactive state primitive (Angular 16+). A signal is a
  // value that tracks who reads it, so when you call title.set(...) Angular
  // re-renders exactly the parts of the template that read it. Read it in a
  // template as {{ title() }} - note the parentheses, it is called like a
  // function.
  protected readonly title = signal('hello-world');

  /* ==== DEPENDENCY INJECTION: THE "INJECTING" SIDE ========================
   * Neither line below ever says new ApiService(). We declare WHAT we need and
   * Angular's injector supplies it. That indirection is the whole point:
   *  - one shared instance instead of everyone making their own,
   *  - the dependency can be swapped for a fake in tests without touching us.
   *
   * Both forms below do the same lookup and, because ApiService is a root
   * singleton, both hand back the very same object. Two styles, one mechanism.
   * ====================================================================== */

  // Style 1: the inject() function. Works in a field initializer because that
  // code runs inside Angular's "injection context" during construction. This is
  // the modern preferred style - no constructor boilerplate, and it composes
  // better with inheritance.
  myService: ApiService = inject(ApiService);//?Injection - field assigned by angular at our request

  // Style 2: constructor parameter injection - the classic form. Angular reads
  // the parameter's TYPE from the metadata the @Component decorator generated,
  // looks that type up as a token in the injector, and passes the instance in.
  // The type annotation is not just documentation here; it IS the lookup key.
  // (Doing both, as this class does, is redundant - the constructor simply
  // overwrites the field with the identical object. Handy for demoing the two
  // styles side by side, but pick one in real code.)
  constructor(apiService: ApiService) {//Injection - param passed in by angular at our request
    this.myService = apiService;
  }



}
