import { Component } from '@angular/core';
import { BookingPanel } from './booking-panel/booking-panel';
import { CapacityDisplay } from './capacity-display/capacity-display';

@Component({
  selector: 'app-root',
  imports: [BookingPanel, CapacityDisplay],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
