import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-two-way-child',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './two-way-child.component.html',
  styleUrl: './two-way-child.component.css'
})
export class TwoWayChildComponent {
  
  @Input() childValue: String | undefined;
  @Output() childValueChange = new EventEmitter<String>();

  changeValue(event: any):void {
    this.childValueChange.emit(event.target.value);
  }


}
