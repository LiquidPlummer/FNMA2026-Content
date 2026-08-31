/* ============================================================================
 * A SIBLING COMPONENT - proof that components are independent units.
 *
 * Two knows nothing about One and vice versa. They are siblings only because
 * app.html happens to render both. If they needed to share data they would do
 * it through a shared service (ApiService) or through @Input/@Output bindings
 * with their parent - never by reaching for each other directly.
 * ========================================================================== */

import { Component } from '@angular/core';

@Component({
  selector: 'app-two',
  imports: [],
  templateUrl: './two.html',
  styleUrl: './two.css',
})
export class Two {

}
