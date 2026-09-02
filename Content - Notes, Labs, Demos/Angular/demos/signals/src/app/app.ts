import { Component, computed, effect, signal, WritableSignal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  count: WritableSignal<number> = signal(0);


  
  //writable signal
  printCount() {
    console.log(this.count())
  }

  incrementCount() {
    this.count.update((x) => {return x + 1})
  }

  resetCount() {
    this.count.set(0)
  }


  //computed signal
  computedDouble = computed(() => {
    return this.count() * 2
  })



  //effect
  effectSignal = effect(() => {
    console.log("count: ", this.count())
  })
}
