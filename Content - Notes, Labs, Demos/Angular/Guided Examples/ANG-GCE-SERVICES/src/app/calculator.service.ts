import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CalculatorService {

  constructor() { }

  a: number = 0;
  b: number = 0;

  add(): number {
    return this.a + this.b;
  }

  subtract(): number {
    return this.a - this.b;
  }

  multiply(): number {
    return this.a * this.b;
  }

  divide(): number {
    return this.a / this.b;
  }

}

