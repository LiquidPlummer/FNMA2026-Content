//Read as: "let x, which is of type number, equal 8"
let num: number = 8
const num2: number = 10

//Read as: "returningFunction, which resolves to type number"
function returningFunction(): number {
    return 5;
}


//array types, like Java, are expressed as "type[]""
let arr: number[] = new Array(1,2,3)
console.log(arr)

//TS enforces these types as build time, but after build JS drops this. But by then the job is done.
let a: number = 1
let b: string = "2.2"
let c: boolean = true

//Any type can be a dangerous crutch, if you write too many ": any" maybe ask yourself if you want to be writing plain JS
let d: any = "literally anything!"
d = 55
d = true

//unknown seems like any, but is very different. 
//any tells TS to stop type checking this thing.
//unknown tells TS to keep type checking - and is a type which can be assigned any value
//however unknown cannot be assigned to anything
let e: unknown = "string"
//let myStr: string = e;//this gives us an error because we haven't narrowed e to a string yet, could be any
if(typeof e == "string") {
    let myStr: string = e
}//TS will infer "narrowing" when you use `typeof` `instanceof` and `in`



let f: void//we don't really do this, this is more for funciton return types
let g: never//similar to void, this is for function return types

let h: object = {}
let i: object[] = [//This was erroring out for no reason earlier?
    {key: "value"}, 
    {username: "kplummer"}, 
    {balance: 55.55, curr: "USD"}
]

//casting with "as" to modify a type ad-hoc
let j: HTMLElement = testFunc() as HTMLElement//without the cast, unknown is not narrow enough to be assigned

function testFunc(): unknown {
    return new HTMLElement()
}


//union types, we can mix several types with the | key
let l: number | string | boolean | object | [] = "8"

//tuples - of any length from 0...many. 
//tuples are immutable in length
//tuples are immutable in value
//tuples are often declared const, then they are fully immutable and safe
const m: [number, number] = [10,22]
let second = m[1]
m[1] = 55;
console.log(second)


//We can create a "Type", which is a type guard. It's not a class, but it does sort of help define objects
type User = {
    id: number,
    name: string
}

let k: User = {id: 2, name: "4"}


interface Dog {name: string}
interface Dog {breed: string}
type Principal = User | Dog | {name: string}

let harry: Dog = {name: "Harry", breed: "german shepard"}

let shadow: Principal = {name: "Shadow"}
let zia: Principal = {name: "Zia", breed: "Choc lab"}
let somebody = {name: "somebody"}



//Typescript has generics just like Java
// Define a generic class named Box
class Box<T> {
  // The content can be any type T
  content: T;

  constructor(value: T) {
    this.content = value;
  }

  getContent(): T {
    return this.content;
  }
}


const stringBox = new Box<string>("Hello TypeScript");
console.log(stringBox.getContent())

