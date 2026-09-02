import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class DataStore {
  sharedValue: String = "DataStore"
}
