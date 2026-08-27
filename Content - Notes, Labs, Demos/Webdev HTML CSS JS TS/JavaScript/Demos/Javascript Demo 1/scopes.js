//What's scope? - domain of where a declared variable exists and is accessible 
//"At runtime when does the variable become accessible, and when does it cease to be"


//Something cannot be in scope before it is declared - except hoisting - all variables get hoisted to the top of their scope
//Something cannot be in scope after the block in which it is declared - it "falls out" of scope

// var - obeys the old scoping rules
// let & const - obey the new scoping rules
// the new scoping rules introduce "block scope"



if(true) {//this is block scope. block scope is not objects, not functions, all other uses of {}
    
    let x = 5;
    var y = 6;
    const z = 7;
    console.log(x)
    
}


console.log(y)// var y
// console.log(x)//let x
// console.log(z)//const z


// console.log(a)


function myFunc() {
    //this is funciton scope
    var a = 1;
    let b = 2;
    const c = 3;
}

// console.log(a)


//lexical scope

function outerFunc() {
    let str = "Hello";

    function innerFunc() {
        console.log(str + " world!")
    }

    function getOut() {
        return innerFunc
    }
}

let extractedFunction = getOut()
extractedFunction()