import { SlicePipe, DatePipe, CurrencyPipe, UpperCasePipe, AsyncPipe } from '@angular/common';
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { interval } from 'rxjs';
import { TruncatePipe } from './truncate-pipe';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SlicePipe, DatePipe, CurrencyPipe, UpperCasePipe, AsyncPipe, TruncatePipe],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('pipes');
  price: number = 123.456789
  created = new Date('2026-09-03');
  name = 'widget';
  tags = ['red', 'blue', 'green'];
  ticks$ = interval(1000);
  description = 'A long product description that runs past the limit';
}
