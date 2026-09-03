import { Component } from '@angular/core';
import { Highlight } from '../highlight';


@Component({
  selector: 'app-custom-directive',
  imports: [Highlight],
  templateUrl: './custom-directive.html',
  styleUrl: './custom-directive.css',
})
export class CustomDirective {

}
