import { Component, signal, SimpleChange, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-child',
  imports: [],
  templateUrl: './child.html',
  styleUrl: './child.css',
})
export class Child {
  renderValue = signal("This is a string")
  label = 'loading…';
  private timer?: ReturnType<typeof setInterval>;


  constructor() {
    // userId is NOT set yet — this prints undefined
    console.log("This is the constructor");
  }
  
  ngOnInit() {
    console.log("ngOnInit")
  }
  

  ngOnChanges() {//TODO: Explore SimpleChanges
    console.log("ngOnChanges")
    
  }


  ngDoCheck() {
    console.log("ngDoCheck")
  }

  ngAfterContentInit() {
    console.log("ngAfterContentInit")
  }

  ngAfterContentChecked() {
    console.log("ngAfterContentChecked")
  }

  ngAfterViewInit() {
    console.log("ngAfterViewInit")
  }

  ngAfterViewChecked() {
    console.log("ngAfterViewChecked")
  }

  afterNextRender() {
    console.log("afterNextRender")
  }

  afterEveryRender() {
    console.log("afterEveryRender")
  }
  
  ngOnDestroy() {
    console.log("ngOnDestroy")
  }

  handleButton() {
    this.renderValue.update((x)=>{return this.renderValue() + 1})
  }
  
  // handleInput(event: any) {
  //   this.renderValue.set(event.target.value)
  // }

}
