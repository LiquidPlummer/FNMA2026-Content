import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from "@angular/router";

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-parent',
  styleUrl: './parent.css',
  templateUrl: './parent.html',
})
export class Parent {

}
