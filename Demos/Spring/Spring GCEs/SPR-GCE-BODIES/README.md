# SPR-GCE-BODIES
In this example we will take a brief look at request and response bodies in Spring web controllers.

If you clone this project and want to run it yourself, remember to get the maven dependencies after cloning with the
CLI command: `mvn install`.

### Version Information
| Software       | Version |
|----------------|---------|
| SpringBoot     | 3.3.5   |
| Spring Web     | 6.1.14  |
| Java           | 21      |

## HTTP Messages
An HTTP exchange is made up of a request and response. The client sends a request for some reason, and the server 
responds in kind. There's lots of ways to include necessary information in these requests and responses, but the most 
important is the body. The body is the main content of the request or response. Headers, URLs, and everything else 
mostly exist to compliment the body of the message.

## Request & Response Bodies
Depending on the type of request (the HTTP method) we can expect to find a request body, or we can expect to send a 
response body. Sometimes both, for instance: 
 - GET requests generally do not have a request body, but are requesting a response body
 - PUT requests generally do contain a body, but do not expect a response body
 - POST requests generally contain and body and expect a response body
Note: In Spring any request or response could contain a body, nothing will stop you from reading a request body from a 
GET request. 

In the ResourceController class we have GET, POST, and PUT examples.

## Working with Bodies in Spring Web
There's not a lot more to it, we just need to be able to read the body of a request, and write a body in a response. 
There are several ways to accomplish these tasks in Spring Web, shown in the ExampleController class.

### Read Request Body Manually
We can read the body right out of the request object. We just need to tell Spring to give us that object, and then we 
get a reader to consume the body:

```Java 
@PostMapping("/readRequestBody")
public void readRequestBody(HttpServletRequest request) throws IOException {
    BufferedReader bufferedReader = request.getReader();
    while(bufferedReader.ready()) {
        System.out.println(bufferedReader.readLine());
    }
}
```
This is a pretty straightforward pattern. Get the reader object, and as long as the `.ready()` method returns true, we 
loop reading the body contents line by line.

### Write Response Body Manually
Similarly, we can manually write the response body using the response object and a PrintWriter:

```Java
@GetMapping("/writeResponseBody")
public void writeResponseBody(HttpServletResponse response) throws IOException {
    String responseText = "{/*...*/}";
    PrintWriter printWriter = response.getWriter();
    printWriter.write(responseText);
}
```
We ask Spring to give us the response object, we call `.getWriter()` on that to get a PrintWriter object with which can 
write information into the response body. Then we call `.write()` and pass our string.

## RESTful Bodies & Resource Representations
The content in bodies can take many formats. In modern times, JSON bodies are very common. JSON (JavaScript Object
Notation) is an interchange format which lends itself to easily representing data objects. For instance, here is the
JSON to represent an object of the Resource class:
```JSON
{
    "firstName":"first",
    "lastName":"last",
    "email":"test@test.com"
}
```
Compare this JSON to the Resource class. We can hold the same information there, a first name, last name, and email
address. This is a "resource representation", both this JSON and the resulting Resource object represent the same
data. These concepts are part of a larger architecture called REST (REpresentational State Transfer).

We can convert JSON into objects, and objects into JSON using the Jackson ObjectMapper. However, this is very common 
and this functionality is already built in to Spring Web. We can tell Spring to convert the JSON in a request body 
into an object for us, and we can tell it to convert an object into JSON for the response body using the 
`@RequestBody` and `@ResponseBody` annotations.

### Reading Representations with @RequestBody
We tell Spring to read the JSON out of a request body and convert it into an object using the `@RequestBody` annotation. 
We just add that annotation to the parameter list and Spring takes care of the rest. Spring uses the Jackson FasterXML 
library here, and all of the usual rules apply to this conversion.

```Java
@PostMapping("/restfulRequestBody")
public void restfulRequestBody(@RequestBody Resource newResource) {
    System.out.println(newResource);
}
```

### Writing Representations with @ResponseBody
Similarly, we can convert an object into a JSON representation and include it in the response with a single annotation. 
We add the @ResponseBody annotation to the return type, telling spring an object of this type will be converted into 
JSON and written into the response body.

```Java
@GetMapping("/restfulResponseBody")
public @ResponseBody Resource restfulResponseBody() {
    return new Resource("first", "last", "test@test.com");
}
```

## ResponseEntity
Spring also has a built-in class called `ResponseEntity`. This object represents the whole response, similar to how the 
`HttpServletRequest` class does. We can write a response body into it in several ways. Take a look at this example:

```Java
@GetMapping("/responseEntity")
public ResponseEntity<Resource> responseEntity() {
    return ResponseEntity.ok(new Resource("first", "last", "test@test.com"));
}
```
This follows a different pattern than the techniques we've seen above. We will explore this class in
greater detail in another demo.