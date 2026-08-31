import { Component } from '@angular/core';
import { CalculatorService } from '../calculator.service';

@Component({
  selector: 'app-multiplier',
  standalone: true,
  imports: [],
  templateUrl: './multiplier.component.html',
  styleUrl: './multiplier.component.css'
})
export class MultiplierComponent {
  calculator: CalculatorService;

  constructor(calculator: CalculatorService) {
    this.calculator = calculator;
  }
}
