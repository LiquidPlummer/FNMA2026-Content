let button = document.getElementById("submitButton") //this is the DOM - Document Object Model

button.addEventListener("click", buttonPressFetch)

async function buttonPressFetch() {
    //we create a promise here
    //Then all further execution IN THIS FUNCTION is paused until resolved or failed
    console.log("there are other things that happen AFTER the promise in this funciton")

    let response = await fetch("https://pokeapi.co/api/v2/pokemon/ditto")
    let body = await response.json()
    console.log(body)
    
}

let jonny = {
    name: "jonny",
    ability: "limber",
    moves: []
}

async function postNewPokemon() {
    console.log("POSTing a new pokemon...")

    let response = await fetch("https://pokeapi.co/api/v2/pokemon/jonny", {
        method: "POST",
        headers: {
            "Content-Length": 88
        }, 
        body: jonny
    })
    let body = await response.json()
    console.log(body)
}

console.log("script2 loaded!")