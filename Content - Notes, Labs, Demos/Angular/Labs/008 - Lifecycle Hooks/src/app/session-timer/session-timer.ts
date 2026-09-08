import { Component, Input, signal } from '@angular/core';

@Component({
  selector: 'app-session-timer',
  imports: [],
  templateUrl: './session-timer.html',
  styleUrl: './session-timer.css',
})
export class SessionTimer {
  @Input() roomName = '';

  secondsElapsed = signal(0);
}
