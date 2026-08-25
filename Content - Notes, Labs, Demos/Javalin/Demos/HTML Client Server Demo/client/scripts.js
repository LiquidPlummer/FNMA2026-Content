//This is how we might import axios here, but importing it into the HTML works just fine still.
//import axios from "https://cdn.jsdelivr.net/npm/axios/+esm";
function buttonPressAxios() {
    console.log("Submit button pressed...")

    const firstName = document.querySelector("#firstName").value
    const lastName = document.querySelector("#lastName").value
    const username = document.querySelector("#username").value
    const password = document.querySelector("#password").value
    const role = document.querySelector("input[name=role]:checked").value

    const user = {
        firstName: firstName,
        lastName: lastName,
        username: username,
        password: password,
        role: role
    }

    //maybe as part of button press we go pull the data from the input elements

    axios.post("http://localhost:7000/users/register", user)
        .then(thenDo)
        .catch(thenCatch)

    console.log("this runs immedately ")
        
}

function thenDo() {
    console.log("Response Received!")
    window.location.href = "viewUser.html"
}

function thenCatch() {
    console.log("There was an error...")
    window.location.href = "registerUser.html"
}

function errorAlert(error) {
    alert("Oops there was an error: " + error)
}

function getUser() {
    const username = document.querySelector("#username").value
    console.log("Fetching user: ", username)

    axios.get("http://localhost:7000/users/" + username)
    // .then(response => displayUser(response))
    .then(response => logUser(response))
    .catch(error => errorAlert(error))
    
}

function logUser(response) {
    console.log(response)
    // console.log(response.data)
    displayUser(response.data)
}

function displayUser(user) {
    document.querySelector('#replaceMe').remove();

    const div = document.createElement('div');
    div.id = 'userDisplay';

    div.innerHTML = `
        <h3>${user.lastName}, ${user.firstName}</h3>
        <p><strong>Username:</strong> ${user.username}</p>
        <p><strong>Role:</strong> ${user.role}</p>
    `;

    document.body.appendChild(div);
}


// function displayUser(user) {
//     document.querySelector('#replaceMe').remove();

//     const div = document.createElement('div');
//     div.id = 'userDisplay';
//     div.style.cssText = `
//         font-family: sans-serif;
//         max-width: 300px;
//         margin: 20px;
//         padding: 16px;
//         border: 1px solid #ccc;
//         border-radius: 8px;
//         box-shadow: 0 2px 4px rgba(0,0,0,0.1);
//     `;

//     div.innerHTML = `
//         <h3 style="margin-top: 0;">${user.lastName}, ${user.firstName}</h3>
//         <p><strong>Username:</strong> ${user.username}</p>
//         <p><strong>Role:</strong> ${user.role}</p>
//     `;

//     document.body.appendChild(div);
// }
console.log("Scripts.js loaded!")