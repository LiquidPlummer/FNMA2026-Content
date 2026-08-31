/* ============================================================================
 * A LEAF COMPONENT - the smallest complete example of the pattern.
 *
 * Even with an empty class body, all four parts of a component are here:
 * the @Component decorator (metadata), a selector, a template file, and styles.
 * ========================================================================== */

import { Component } from '@angular/core';

@Component({
  selector: 'app-one-child',

  // Empty because this template uses no other components, directives or pipes.
  // The moment you use *ngIf, a pipe, or another component in one-child.html,
  // the thing providing it has to be listed here.
  imports: [],

  templateUrl: './one-child.html',
  styleUrl: './one-child.css',
})
export class OneChild {

}
