import { Component } from '@angular/core';

@Component({
  selector: 'app-one-way-binding',
  standalone: true,
  imports: [],
  templateUrl: './one-way-binding.component.html',
  styleUrl: './one-way-binding.component.css'
})
export class OneWayBindingComponent {
  colorString: String = "black";

  red(): void {
    this.colorString = "red";
  }

  green(): void {
    this.colorString = "green";
  }

  blue(): void {
    this.colorString = "blue";
  }

  black(): void {
    this.colorString = "black";
  }
}
