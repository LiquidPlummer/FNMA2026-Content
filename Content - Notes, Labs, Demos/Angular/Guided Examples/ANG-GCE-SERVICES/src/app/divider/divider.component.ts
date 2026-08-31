import { Component, inject } from '@angular/core';
import { CalculatorService } from '../calculator.service';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-divider',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './divider.component.html',
  styleUrl: './divider.component.css'
})
export class DividerComponent {
  calculator: CalculatorService = inject(CalculatorService);
}
