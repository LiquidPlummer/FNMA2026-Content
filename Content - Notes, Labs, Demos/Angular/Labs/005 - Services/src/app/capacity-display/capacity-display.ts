import { Component, computed, signal } from '@angular/core';

@Component({
  selector: 'app-capacity-display',
  imports: [],
  templateUrl: './capacity-display.html',
  styleUrl: './capacity-display.css',
})
export class CapacityDisplay {
  capacity = 8;
  seatsBooked = signal(0);

  seatsRemaining = computed(() => this.capacity - this.seatsBooked());
}
