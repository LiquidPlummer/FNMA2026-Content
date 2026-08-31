import { Component } from '@angular/core';


@Component({
  selector: 'app-child',
  imports: [],
  templateUrl: './child.html',
  styleUrl: './child.css',
})
export class Child {
  //Three data binding techniques
  //two of these are "back to front"
  //one of these is "front to back"
  sentence: String = "When Kyle says 'front' and 'back', this is the back. This string is declared in the TS component class."
  name: String = "Kyle"//apieService.getUser().response.body.json().name
  reimbursementObj: any = {amount: 55.55, type: "FOOD"}

  //The simplest version of "back to front" data binding is: String Interpolation
}
