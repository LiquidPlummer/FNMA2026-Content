import { Component } from '@angular/core';
import { TwoWayChildComponent } from '../two-way-child/two-way-child.component';
import { FormsModule, NgModel } from '@angular/forms';

@Component({
  selector: 'app-two-way-binding',
  standalone: true,
  imports: [TwoWayChildComponent, FormsModule],
  templateUrl: './two-way-binding.component.html',
  styleUrl: './two-way-binding.component.css'
})
export class TwoWayBindingComponent {
  textString: String = "Text";
}
