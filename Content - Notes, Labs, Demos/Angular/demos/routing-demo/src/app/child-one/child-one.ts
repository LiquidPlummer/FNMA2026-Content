import { Component } from '@angular/core';

@Component({
  imports: [],
  selector: 'app-child-one',
  styleUrl: './child-one.css',
  templateUrl: './child-one.html',
})
export class ChildOne {
  str: String = "Hello"
}
//    {{string interpolation}} - {{render the result of a JS expression}} {{2+2}}
//    [property binding] - bind to an html element property/attribute
//    (event) binding - event listeners for target and event type with handler function

//    two-way data binding [(banana-in-a-box)]



