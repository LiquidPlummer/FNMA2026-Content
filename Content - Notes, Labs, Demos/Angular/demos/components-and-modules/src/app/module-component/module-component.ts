import { Component } from '@angular/core';
import { ExampleModule } from '../example/example-module';

@Component({
  selector: 'app-module-component',
  imports: [ExampleModule],//Because we exported the components in the module, they are all made available to us here. We don't need to import them individually, we just import the module.
  templateUrl: './module-component.html',
  styleUrl: './module-component.css',
})
export class ModuleComponent {

}
