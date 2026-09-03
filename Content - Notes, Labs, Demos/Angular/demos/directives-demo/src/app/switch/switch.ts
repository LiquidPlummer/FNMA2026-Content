import { Component } from '@angular/core';

@Component({
  selector: 'app-switch',
  imports: [],
  templateUrl: './switch.html',
  styleUrl: './switch.css',
})
export class Switch {
  predicate = true

    toggle() {
    if(this.predicate) {
      this.predicate = false
    } else {
      this.predicate = true
    }

    console.log("predicate is: ", this.predicate)
  }
}
