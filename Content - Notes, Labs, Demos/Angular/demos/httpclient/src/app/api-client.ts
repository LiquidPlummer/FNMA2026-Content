import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

  type Pokemon = {
    abilities: any[],
    cries: any
  }

@Injectable({
  providedIn: 'root',
})
export class ApiClient {
  client = inject(HttpClient)
  poke$: Pokemon = {abilities: [], cries: {}}
  


  getPokeApi() {
    this.client.get<Pokemon>("https://pokeapi.co/api/v2/pokemon/ditto", {observe: "body", headers: {}}).subscribe(result => {//We're just passing the "next:" callback
      console.log(result)
    })

    this.client.get<Pokemon>("https://pokeapi.co/api/v2/pokemon/nonsense").subscribe({//this one should generate an error
      next(result) {console.log(result)},
      error(error) {console.log("error: ", error)},
      complete() {console.log("Complete")}
      })


  }
}
