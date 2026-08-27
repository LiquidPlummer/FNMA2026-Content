// different parts of elements that nodes but not elements
// Keep in mind user input afecting the InnerHTML is a vector for injection
// InnerHTML is parsable HTML

//remove elements - so they are no longer part of the dom - no longer appear on screen
//Create elements - then add them into the dom - new elements get rendered on screen
//put the newly created element in the dom
//now we can simulate "navigation" between documents
//What happens if I do these two things together? What might we be able to do now?
//Single Page Application


let clickableSpan = document.getElementById("clickableSpan")
clickableSpan.addEventListener("click", navigate)

function navigate(e) {
    console.log(e.target)
    document.body.innerHTML = `
    <div>
       <script>console.log("This is injected!")</script>
        <p>Hello!</p>
    </div>
    `
}

//How might we have behavior that deletes any element the user clicks?
//find out what they clicked? e.target on the click event
//finally we should be able to call .remove() on the element

document.getElementById("thisDiv").addEventListener("click", changeThings)
document.getElementById("thatDiv").addEventListener("click", changeThings)
document.getElementById("thirdDiv").addEventListener("click", changeThings)

//The three non-element nodes: children of all elements: textContent, innerText, innerHTML
//innerHTML gets parsed and rendered
//innerText rendered text that respects CSS
//textContent is just text and fast and safe

function changeThings(e) {
    let element = e.target
    element.setAttribute("style", "border-style: dashed;")
    // let parent = e.target.parentElement
    // element.remove()
    // let para = document.createElement("p")
    // para.innerText = "This is the new paragraph!"
    // parent.append(para)
}

document.getElementById("theButton").addEventListener("click", addToList)

function addToList() {
    const li = document.createElement("li");
    li.textContent = "New item";
    li.classList.add("item", "item--new");
    document.querySelector("#list").append(li);
}
