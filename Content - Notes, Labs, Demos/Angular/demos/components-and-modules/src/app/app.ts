import { Component, signal } from '@angular/core';
import { ModuleComponent } from "./module-component/module-component";

@Component({
  selector: 'app-root',
  imports: [ModuleComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('components-and-modules');
}
