import { Component } from '@angular/core';
import { SessionMonitor } from './session-monitor/session-monitor';

@Component({
  selector: 'app-root',
  imports: [SessionMonitor],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
