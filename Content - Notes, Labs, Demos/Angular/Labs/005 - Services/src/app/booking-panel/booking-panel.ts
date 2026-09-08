import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-booking-panel',
  imports: [],
  templateUrl: './booking-panel.html',
  styleUrl: './booking-panel.css',
})
export class BookingPanel {
  capacity = 8;
  seatsBooked = signal(0);

  book() {
    this.seatsBooked.update((n) => Math.min(n + 1, this.capacity));
  }

  cancel() {
    this.seatsBooked.update((n) => Math.max(n - 1, 0));
  }
}
