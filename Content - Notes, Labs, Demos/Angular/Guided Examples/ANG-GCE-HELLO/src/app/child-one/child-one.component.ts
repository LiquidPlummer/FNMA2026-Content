import { Component } from '@angular/core';

@Component({
  selector: 'app-child-one',
  standalone: true,
  imports: [],
  templateUrl: './child-one.component.html',
  styleUrl: './child-one.component.css'
})
export class ChildOneComponent {
  constructor () {
    console.log("This is the child one component, ChildOneComponent");
  }
}
