import { Component } from '@angular/core';
import { Member } from '../member';

@Component({
  selector: 'app-profile-editor',
  imports: [],
  templateUrl: './profile-editor.html',
  styleUrl: './profile-editor.css',
})
export class ProfileEditor {
  member: Member = {
    name: 'Sam Ortiz',
    email: 'sortiz@riverbend.edu',
    role: 'Student',
  };
}
