import { Directive, ElementRef, inject, input } from '@angular/core';

@Directive({
  selector: '[appHighlight]',
  host: {
    '(mouseenter)': 'onEnter()',
    '(mouseleave)': 'onLeave()',
  },
})
export class Highlight {

  constructor() { }

  private el = inject(ElementRef);
  appHighlight = input('yellow');  // ← matches the selector

  onEnter() { 
    this.el.nativeElement.style.backgroundColor = this.appHighlight()
  }
  
  onLeave() { 
    this.el.nativeElement.style.backgroundColor = ''
  }
}
