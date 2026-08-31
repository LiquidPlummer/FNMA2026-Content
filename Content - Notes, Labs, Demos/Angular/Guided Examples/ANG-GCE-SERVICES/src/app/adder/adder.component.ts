import { Component } from '@angular/core';
import { CalculatorService } from '../calculator.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-adder',
  standalone: true,
  /* Importing a service in this way is no longer supported in the new standalone paradigm.
  Lucky for us our service is providedIn: 'root' so it's already available here anyway. In
  a non-standalone project (any before v17) we could import the service here.*/
  imports: [/*CalculatorService*/],
  templateUrl: './adder.component.html',
  styleUrl: './adder.component.css'
})
export class AdderComponent {
  
  calculator: CalculatorService;

  constructor(calculator: CalculatorService) {
    this.calculator = calculator;
  }
}
