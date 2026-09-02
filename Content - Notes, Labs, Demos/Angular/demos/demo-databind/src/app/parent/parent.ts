import { Component } from '@angular/core';
import { Child } from "../child/child";
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-parent',
  imports: [Child, FormsModule],
  templateUrl: './parent.html',
  styleUrl: './parent.css',
})
export class Parent {
  name: string = "Kyle"
  age: number = 41
  color: string = "red"
  textString: String = "Hello"

  handleEvent(str: String) {
    console.log(str)
  }
}
