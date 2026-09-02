import { Component, inject } from '@angular/core';
import { DataStore } from '../data-store';

@Component({
  selector: 'app-distant-component',
  imports: [],
  templateUrl: './distant-component.html',
  styleUrl: './distant-component.css',
})
export class DistantComponent {
  dataStore: DataStore

  constructor(dataStore: DataStore) {
    this.dataStore = dataStore
  }

  handleInput(event: any) {
    this.dataStore.sharedValue = event.target.value
  }

}
