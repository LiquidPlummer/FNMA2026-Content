import { NgStyle } from '@angular/common';
import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-ngstyle-attribute',
  imports: [NgStyle],
  templateUrl: './ngstyle-attribute.html',
  styleUrl: './ngstyle-attribute.css',
})
export class NgstyleAttribute {
  color = signal("green")
}
