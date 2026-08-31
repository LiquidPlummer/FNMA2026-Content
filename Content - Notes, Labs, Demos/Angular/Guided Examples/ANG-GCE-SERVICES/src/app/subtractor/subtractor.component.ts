import { Component, inject } from '@angular/core';
import { CalculatorService } from '../calculator.service';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-subtractor',
  standalone: true,
  imports: [],
  templateUrl: './subtractor.component.html',
  styleUrl: './subtractor.component.css'
})
export class SubtractorComponent {
  calculator: CalculatorService = inject(CalculatorService);
}
