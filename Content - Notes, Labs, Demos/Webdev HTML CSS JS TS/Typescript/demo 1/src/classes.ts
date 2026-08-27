class Account {
    private id: number
    protected bal: number = 0.0
    private static count: number = 0;

    public constructor(id: number) {
        Account.count++;
        this.id = id
    }

    public getId(): number {
        return this.id;
    }

    public setId(id: number) {
        this.id = id;
    }

    public checkCount(): number {
        return Account.count;
    }

    
}


let z = new Account(1)
let y = new Account(2)
let x = new Account(3)
console.log(x.checkCount())



class SavingsAccount extends Account {
    private readonly interestRate: number =  1.01

    applyInt(): void {
        this.bal = this.bal * this.interestRate
    }
}
let w = new SavingsAccount(1)
let v = new SavingsAccount(2)
let u = new SavingsAccount(3)
console.log(u.checkCount())


abstract class Shape {
  abstract area(): number;
  describe() { return `Area: ${this.area()}` }
}
// new Shape();  //cant instantiate this, only for inheritance


interface Greeter {
    name: string;
    greet(): string
}

class En implements Greeter {
    name: string;
    constructor(name: string) {
        this.name = name;
    }

    greet() { 
        return `Hi ${this.name}`
    }
}

class Fr implements Greeter {
    name: string;

    constructor(name: string) {
        this.name = name;
    }
    
    greet() { 
        return `Bonjour ${this.name}` 
    }
}

let greeter: Greeter = new Fr("Kyle");
console.log(greeter.greet())

greeter = new En(greeter.name)
console.log(greeter.greet())


