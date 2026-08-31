import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChildOneComponent } from './child-one/child-one.component';
import { ChildTwoComponent } from './child-two/child-two.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ChildOneComponent, ChildTwoComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  constructor () {
    console.log("This is the root component, AppComponent");
  }
}
