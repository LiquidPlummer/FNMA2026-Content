import { Component, signal } from '@angular/core';
import { SessionTimer } from '../session-timer/session-timer';

const ROOMS = ['Study Room 204', 'Study Room 118', 'The Annex'];

@Component({
  selector: 'app-session-monitor',
  imports: [SessionTimer],
  templateUrl: './session-monitor.html',
  styleUrl: './session-monitor.css',
})
export class SessionMonitor {
  roomName = signal(ROOMS[0]);
  timerVisible = signal(true);

  nextRoom() {
    const current = ROOMS.indexOf(this.roomName());
    this.roomName.set(ROOMS[(current + 1) % ROOMS.length]);
  }

  toggleTimer() {
    this.timerVisible.update((visible) => !visible);
  }
}
