import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CharizardComponent } from './charizard/charizard.component';
import { MewtwoComponent } from './mewtwo/mewtwo.component';
import { PikachuComponent } from './pikachu/pikachu.component';
import { AppComponent } from '../app.component';



@NgModule({
  declarations: [AppComponent, CharizardComponent, MewtwoComponent, PikachuComponent],
  imports: [
    CommonModule
  ],
  exports: [

  ]
})
export class PokemonModule { }
