//This is setting up event listeners, basically simple DOM manipulation
let button = document.getElementById("submitButton") //this is the DOM - Document Object Model
button.addEventListener("click", buttonPressFetch)

let jonny = {
    name: "jonny",
    ability: "limber",
    moves: []
}






async function buttonPressFetch() {
    //we create a promise here
    //Then all further execution IN THIS FUNCTION is paused until resolved or failed
    console.log("there are other things that happen AFTER the promise in this funciton")





    let response = await fetch("https://pokeapi.co/api/v2/pokemon/ditto")//many times per second the JS engine is checking if the promise is fulfilled or rejected, and thus if we can continue IN THIS FUNCTION
    let body = await response.json()

    console.log(body)
    
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