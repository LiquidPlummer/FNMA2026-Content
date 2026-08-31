import { Component } from '@angular/core';

@Component({
  selector: 'app-child-two',
  standalone: true,
  imports: [],
  templateUrl: './child-two.component.html',
  styleUrl: './child-two.component.css'
})
export class ChildTwoComponent {
  constructor () {
    console.log("This is the child two component, ChildTwoComponent");
  }
}
