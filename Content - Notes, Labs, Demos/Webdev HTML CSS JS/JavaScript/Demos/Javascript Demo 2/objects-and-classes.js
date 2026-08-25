const { StrictMode } = require("react");

let modelAndDao = {
    name: "Kyle",
    password: "pass123",
    save: () => {/*this is make-believe*/}
}


class ModelAndDao {
    name;
    #secret = "This is a secret value"

    constructor(name) {
        this.name = name;
    }

    greet() {
        console.log(`Hello, ${this.name}`)
    }

    spillTheBeans() {
        console.log(this.#secret)
    }
}


let myUser = new ModelAndDao("Kyle")
myUser.name = "Phil"
myUser.greet()
let myName = myUser.name;

myUser.spillTheBeans()
