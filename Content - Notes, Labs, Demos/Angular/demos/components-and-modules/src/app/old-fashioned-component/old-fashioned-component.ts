import { Component } from '@angular/core';

@Component({
  standalone: false,//This one is not standalone, this is how components used to be, they had to be part of a module.
  selector: 'app-old-fashioned-component',
  templateUrl: './old-fashioned-component.html',
  styleUrl: './old-fashioned-component.css',
})
export class OldFashionedComponent {

}
