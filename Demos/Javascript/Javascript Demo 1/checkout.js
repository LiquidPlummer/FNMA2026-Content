// checkout.js
const form = {
    price: "19.99",
    quantity: "3",
    discountCode: "save10",
    calculateSubtotal: function() {return this.price * this.quantity}
}





//Create a variable for tax rate...
const TAX_RATE = 1.0825
const DISCOUNT_RATE = .9


//q * p * dis * tax = total
let total = form.price * form.quantity * DISCOUNT_RATE * TAX_RATE


console.log(typeof form)                  // object
console.log(typeof form.price)            // number
console.log(typeof form.quantity)         // number
console.log(typeof form.discountCode)     // string
console.log("total: ", total, typeof total)
console.log(typeof true)


let x = 0;
let y = "" + 0;
console.log("" + x, typeof ("" + 0))
console.log(x, typeof x)


console.log(x == "0") //true with coercion - lose equality or "equivalence" checking
console.log(x === "0")//false with strict equality checking - non-coercive
//this is equivalent to:
console.log(typeof x == typeof "0" && x == "0")


//Truthy & Falsy - Can we check if something is "almost true"? or "true enough"? "true-like"
//these are the coercion rules for booleans


let undef;//undefined
let nul = null;

if(!0) {
    console.log("Then 0 is a falsy value")
}

if(!nul) {
    console.log("Then null is a falsy value")
}

if(!undef) {
    console.log("Then undefined is a falsy value")
}

if(!"") {
    console.log("Then empty string is a falsy value")
}


if(!NaN) {
    console.log("Then NaN is a falsy value")
}


if(!{}) {
    console.log("Then {} is a falsy value")
}


//null
//undefined
//empty string ""
//0
//NaN
//false



//take in the user input from a text field, but it should be a number. "19.99"
let a = Number("19.99")
console.log("Change the type: ", typeof a);

let b = myFunc("hello")

console.log("When does this occur?")

function myFunc() {
    console.log("??????")
}

// console.log(hoistMe)
// hoiseMe = 6;
// console.log(hoistMe)


//First Order Variables
let myOtherFunc = myFunc;
let myArrowFunc = () => {console.log("??????")}


let myThirdFunc = function(x) {return x*2}

let result = myThirdFunc(5)
console.log(result)



console.log(form.calculateSubtotal())