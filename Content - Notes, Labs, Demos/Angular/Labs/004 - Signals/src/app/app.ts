import { Component } from '@angular/core';
import { ProfileEditor } from './profile-editor/profile-editor';

@Component({
  selector: 'app-root',
  imports: [ProfileEditor],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
