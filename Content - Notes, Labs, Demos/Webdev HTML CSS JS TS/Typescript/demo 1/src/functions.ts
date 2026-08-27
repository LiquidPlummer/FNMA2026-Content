function identifier(): string {
    return "Hello"
}

function myFunction(name: string, age: number = -1, favColor?: string): void {
    console.log(`name: ${name}\nage: ${age}\n${favColor}`)
}

function add(a: number, b: number, ...c: number[]) {//rest operator "The 'rest' of the params. This is like Java Varargs
    let sum = a + b;
    for(let i = 0; i < c.length; i++) {
        sum += c[i];
    }
    return sum;
}

myFunction("Kyle", undefined, undefined)
console.log("sum: ", add(1,2,3,4,5))



let nums: [number, number, number] = [1,2,3];
console.log(add(...nums))