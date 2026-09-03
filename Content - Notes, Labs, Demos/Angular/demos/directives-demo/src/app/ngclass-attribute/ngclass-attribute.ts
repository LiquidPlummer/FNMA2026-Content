import { NgClass } from '@angular/common';
import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-ngclass-attribute',
  imports: [NgClass],
  templateUrl: './ngclass-attribute.html',
  styleUrl: './ngclass-attribute.css',
})
export class NgclassAttribute { 
  isActive = signal(true)
}
