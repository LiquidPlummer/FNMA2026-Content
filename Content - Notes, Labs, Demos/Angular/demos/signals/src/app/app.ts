import { Component, computed, effect, linkedSignal, model, ModelSignal, signal, untracked, WritableSignal } from '@angular/core';
import { RouterOutlet } from '@angular/router';


//model signal
// A model() is a writable signal that is ALSO an input/output pair. Angular
// pairs the input `value` with an output `valueChange` behind the scenes,
// which is exactly what the [( )] "banana in a box" syntax looks for. So a
// parent can two-way bind to it: the parent can write it, the child can write
// it, and both stay in sync with no wiring in between.
//
// That handshake is the whole point of model(), so it needs a child component
// to show off. This one lives above App because App's `imports` array below
// references it, and the class has to exist before that array is evaluated.
@Component({
  selector: 'app-child',
  template: `
    <fieldset>
      <legend>child component</legend>
      child sees: {{ this.value() }}
      <button (click)="this.childIncrement()">Child Increment</button>
    </fieldset>
  `
})
export class Child {

  // The 0 is the DEFAULT, used only if the parent never binds anything.
  // Use model.required<number>() to force the parent to supply a value.
  value: ModelSignal<number> = model(0);

  childIncrement() {
    this.value.update((x) => {return x + 1})
  }
}


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Child],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  count: WritableSignal<number> = signal(0);


  
  //writable signal
  // The basic building block: a box holding a value that also remembers who
  // read it. You READ it by calling it like a function, count(), and you WRITE
  // it with .set(newValue) or .update(oldValue => newValue).
  //
  // The remembering is the important part. When you write to it, Angular knows
  // every template, computed, and effect that read it, and refreshes exactly
  // those. Nothing else on the page is re-checked.
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
  // A DERIVED, read-only signal. You give it a recipe instead of a value, and
  // it figures out its own dependencies from whichever signals the recipe
  // actually reads.
  //
  // It is lazy and cached: the recipe does not run until something reads
  // computedDouble(), and after that the answer is reused until one of those
  // dependencies actually changes. There is no .set() here, and that is the
  // point. The only way to move this number is to move count.
  computedDouble = computed(() => {
    return this.count() * 2
  })



  //effect
  // For SIDE EFFECTS: reaching outside the reactive world to log, touch
  // localStorage, talk to a non-signal library. Like computed, it tracks
  // whatever signals it reads and re-runs when they change, but it returns
  // nothing and nobody can read it.
  //
  // It runs once up front to discover its dependencies, then re-runs on
  // change. Note it is SCHEDULED, not synchronous, so the log lands slightly
  // after the click, and several rapid changes may collapse into one run.
  // Rule of thumb: do not write to signals in here, use computed for that.
  effectSignal = effect(() => {
    console.log("count: ", this.count())
  })



  //linkedSignal
  // The in-between case: a computed you are allowed to WRITE to. It starts
  // out derived from its source, and .set() overrides that value, but the
  // moment the source changes it throws your override away and recomputes.
  //
  // Reach for this when local state needs to be reset by something upstream:
  // a form field that repopulates when you pick a different record, a "rows
  // per page" that resets when the filter changes, a selected item that has
  // to be dropped when the list it came from is replaced.
  linkedCount = linkedSignal(() => {
    return this.count() + 10
  })

  overrideLinked() {
    this.linkedCount.set(999)   // computed() would not allow this
  }



  //untracked
  // An escape hatch for READING a signal without subscribing to it. Normally,
  // any signal read inside a computed or effect becomes a dependency of it.
  // Wrapping the read in untracked() gets you the current value while staying
  // invisible to the dependency tracking.
  //
  // Use it when a value is an ingredient but not a trigger: extra context you
  // want included in a log, a config or user id you need to read but do not
  // want to re-fire on. The effect below re-runs when count changes, but NOT
  // when nickname changes, even though it prints the nickname.
  nickname: WritableSignal<string> = signal("Ada");

  changeNickname() {
    this.nickname.set(this.nickname() === "Ada" ? "Grace" : "Ada")
  }

  untrackedEffect = effect(() => {
    const name = untracked(() => {return this.nickname()})
    console.log(`${name} has counted to ${this.count()}`)
  })



  //model signal (the Child component above holds the model)
  // From the parent's side there is nothing special to declare. This is an
  // ordinary writable signal, handed to the child in app.html with
  // [(value)]="this.sharedValue". Both components now share one value.
  sharedValue: WritableSignal<number> = signal(0);

  parentIncrement() {
    this.sharedValue.update((x) => {return x + 1})
  }
}
