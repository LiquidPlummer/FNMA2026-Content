import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Parent } from './parent/parent';
import { Child } from './child/child';
import { PropertyBinding } from "./property-binding/property-binding";
import { EventBinding } from "./event-binding/event-binding";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Parent, Child, PropertyBinding, EventBinding],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('demo-databind');
}
