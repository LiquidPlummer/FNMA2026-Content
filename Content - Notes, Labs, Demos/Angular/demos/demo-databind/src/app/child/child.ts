import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DataStore } from '../data-store';


@Component({
  selector: 'app-child',
  imports: [],
  templateUrl: './child.html',
  styleUrl: './child.css',
})
export class Child {

  @Input() name: String | undefined
  @Output() nameChange: EventEmitter<String> = new EventEmitter<String>();
  dataStore: DataStore


  constructor(dataStore: DataStore) {
    this.dataStore = dataStore
  }



  emitChanges(event: any) {
    // console.log("Event emitting from child...")
    this.nameChange.emit(event.target.value)
  }




  
}
