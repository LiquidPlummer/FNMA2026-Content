import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { For } from './for/for';
import { If } from './if/if';
import { Switch } from './switch/switch';
import { NgclassAttribute } from './ngclass-attribute/ngclass-attribute';
import { NgClass } from '@angular/common';
import { NgstyleAttribute } from './ngstyle-attribute/ngstyle-attribute';
import { CustomDirective } from './custom-directive/custom-directive';

@Component({
  selector: 'app-root',
  imports: [For, If, Switch, NgclassAttribute, NgstyleAttribute, CustomDirective],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('directives-demo');


}
