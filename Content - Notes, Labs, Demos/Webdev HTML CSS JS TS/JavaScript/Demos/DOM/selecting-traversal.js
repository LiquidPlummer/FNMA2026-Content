let thisDiv = document.getElementById("thisDiv")
let theseDivs = document.getElementsByClassName("divClass")
let allDivs = document.getElementsByTagName("div")

console.log("thisDiv", thisDiv)
console.log("theseDivs", theseDivs)
console.log("allDivs", allDivs)



let allElements = document.querySelectorAll("*")    //many
thisDiv = document.querySelector("#thisDiv")        //single
theseDivs = document.querySelectorAll(".divClass")  //many
allDivs = document.querySelectorAll("div")          //many

console.log("thisDiv", thisDiv)
console.log("theseDivs", theseDivs)
console.log("allDivs", allDivs)
console.log("all elements", allElements)


//Traversing the dom
let parentOfThisDiv = thisDiv.parentElement
let parentNode = thisDiv.parentNode

//fact finding - inteliogence gathering
console.log("parentElement", parentOfThisDiv)
console.log("parentNode", parentNode)


let body = document.body
let childDivs = body.children
let firstChild = body.firstElementChild
let lastChild = body.lastElementChild
console.log("childDivs", childDivs)

let middleOne = firstChild.nextElementSibling
let middleTwo = lastChild.previousElementSibling.previousElementSibling
if(middleOne === middleTwo) {
    console.log("We got the middle element!")
}


let matchedElement = middleOne.closest("body")
console.log("matchedElement: ", matchedElement)
let result = matchedElement.matches("#bodyId")
console.log(result)

//Using closest() to get the nearest clickable div based on it's class name
let span = document.querySelector("#clickableSpan")
span.addEventListener("click", eventHandler)

function eventHandler(e) {//The event object is implicit in JS, it won't be in Ang?
    let clickableSection = e.target.closest(".clickableSection")
    console.log(clickableSection)
}