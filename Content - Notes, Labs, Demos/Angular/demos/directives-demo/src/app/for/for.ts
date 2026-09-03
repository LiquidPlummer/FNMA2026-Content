import { Component } from '@angular/core';

@Component({
  selector: 'app-for',
  imports: [],
  templateUrl: './for.html',
  styleUrl: './for.css',
})
export class For {
  fruitList = [{id: 3, name: "pears"}, {id: 1, name: "oranges"}, {id: 2, name: "apples"}]
}
