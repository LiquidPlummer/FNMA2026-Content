import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Member } from '../member';

@Component({
  selector: 'app-member-card',
  imports: [FormsModule],
  templateUrl: './member-card.html',
  styleUrl: './member-card.css',
})
export class MemberCard {
  @Input() member!: Member;
  @Output() roleChange = new EventEmitter<string>();
  draftRole = '';

  applyRole() {
    this.roleChange.emit(this.draftRole);
    this.draftRole = '';
  }
}
