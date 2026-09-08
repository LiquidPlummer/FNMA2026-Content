import { Component } from '@angular/core';
import { MemberCard } from '../member-card/member-card';
import { Member } from '../member';

@Component({
  selector: 'app-profile-editor',
  imports: [MemberCard],
  templateUrl: './profile-editor.html',
  styleUrl: './profile-editor.css',
})
export class ProfileEditor {
  member: Member = {
    name: 'Sam Ortiz',
    email: 'sortiz@riverbend.edu',
    role: 'Student',
  };

  lastChange = 'No changes yet.';

  onRoleChange(newRole: string) {
    this.member.role = newRole;
    this.lastChange = `Role changed to ${newRole}.`;
  }
}
