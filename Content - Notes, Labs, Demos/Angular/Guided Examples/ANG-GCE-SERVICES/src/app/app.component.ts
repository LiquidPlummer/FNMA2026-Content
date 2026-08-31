import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { CalculatorService } from './calculator.service';
import { AdderComponent } from './adder/adder.component';
import { SubtractorComponent } from './subtractor/subtractor.component';
import { MultiplierComponent } from './multiplier/multiplier.component';
import { DividerComponent } from './divider/divider.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    RouterOutlet, 
    FormsModule,
    AdderComponent,
    SubtractorComponent,
    MultiplierComponent,
    DividerComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  calculator: CalculatorService;

  constructor(calculator: CalculatorService) {
    this.calculator = calculator;
  }


}
