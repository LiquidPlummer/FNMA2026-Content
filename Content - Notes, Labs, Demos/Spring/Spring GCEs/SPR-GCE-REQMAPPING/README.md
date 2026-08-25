# SPR-GCE-REQMAPPING
In this example we will explore controller classes, mapping web requests to handler methods, and working with requests 
and responses. 

If you clone this project and want to run it yourself, remember to get the maven dependencies after cloning with the 
CLI command: `mvn install`.

### Version Information
| Software       | Version |
|----------------|---------|
| SpringBoot     | 3.3.5   |
| Spring Web     | 6.1.14  |
| Java           | 21      |

## Controllers
Controllers in spring are a type of Bean. This special type of Bean interacts with web requests. Spring controller 
Beans contain request handlers, methods that respond to a specific type of request. The Spring server "listens" for 
incoming requests, and routes them to the appropriate controller and handler as they come in.

Often, controllers are designed with a specific resource in mind. All of the methods in a controller will operate on the 
same resource. We could have a `UserController` which is designed to handle requests pertaining to `User` objects. We 
could have separate methods for the different HTTP operations on that resource, GET, POST, PUT, PATCH, and DELETE.

In the `ResourceController` we have several methods which allow clients to send GET and POST requests.

## Request Handlers
A controller is made up of methods which handle specific requests, helpfully called "request handlers". Often handlers 
which operate on the same resources are grouped together into a class. The controller handles all requests for a 
particular resource, and each method handles a specific type request on that resource.

### Mapping
We need to map requests to methods in order to make our server work, just like we would with similar server-side 
technologies like Javalin, or Java Servlets. In this case we tell Spring what URL and HTTP method lead to what handler. 
We do this by annotating handler methods with generic `@RequestMapping`, or the more specific `@GetMapping`, 
`@PostMapping`, `@PutMapping`, `@PatchMapping`, and `@DeleteMapping`

`@RequestMapping` will take in at least two attributes, `method` and `path`. The others imply their methods, and only 
require one `path` attribute. For example:

```Java
@RequestMapping(method = RequestMethod.GET, path = "/example")
```

```Java
@PostMapping("/resources")
```

## Working with Requests and Responses
HTTP requests and responses are standardized, and are the same regardless of technology. That means these request 
handler methods are doing the same things that other technologies do to handle web traffic. Reading headers, 
bodies, paths, and parameters from the request, then preparing a response with headers, body, and a status code. There 
are lots of mechanisms in place to perform these operations, in this example we will cover some of it in brief. There 
is a lot more to explore, and we will delve deeper in later examples.

### HttpServletRequest and HttpServletResponse
In `ResourceController` we have the `exampleHandler` method, which appears below:

```Java
@RequestMapping(method = RequestMethod.GET, path = "/example")
public void exampleHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //Extract the request body JSON:
    BufferedReader reader = request.getReader();
    StringBuilder stringBuilder = new StringBuilder();
    while(reader.ready()) {
        stringBuilder.append(reader.readLine());
    }

    //Convert JSON into a Resource object with Jackson ObjectMapper:
    ObjectMapper objectMapper = new ObjectMapper();
    Resource newResource = objectMapper.readValue(stringBuilder.toString(), Resource.class);

    //Add the new resource to the list:
    this.resources.add(newResource);

    //Prepare the response:
    response.setStatus(202);
    PrintWriter printWriter = response.getWriter();
    printWriter.print(objectMapper.writeValueAsString(newResource));
    }
```

There's a lot here, let's break it down. This may seem familiar if you've ever worked with low-level web libraries like 
Java Servlets. We start by mapping GET requests to the "/example" path to this handler using the `@RequestHandler` 
annotation.

```Java 
@RequestMapping(method = RequestMethod.GET, path = "/example")
```

We then enter into the method. When a GET request is sent to "www.site.com/example" Spring invokes our method, and 
it begins executing. This method has two parameters: `HttpServletRequest request`, and `HttpServletResponse response`. 
These tell Spring that we want the request and response objects. We won't always need them, but sometimes we do. This 
works very much like autowiring, when Spring notices we declared the method with those parameters, it knows to pass them 
into the method.

Once we have those two objects we can start processing the request and preparing a response. We call `getReader()` on 
the request which gives us a `BufferedReader` object with which we can extract the request body text. We do this in a 
loop: as long as the buffer is `ready()` we loop and `readLine()`.

In order to fulfil this request we convert the JSON text from the request body into a Resource object and add it to the 
list with the rest. 

After we are done processing the request and taking care of any work we must accomplish, we can start preparing a 
response. We set the status code on the `HttpServletResponse` object by calling `setStatus()`, then we do the opposite 
of the work we did earlier, converting our Resource object back into JSON and using a `PrintWriter` object to write that 
into the response.

Now that the work is done and the response is ready we are done! We don't even need to return anything. Spring will send 
the response without us telling it to.

### High-Level
The whole point of complicated frameworks like Spring is to abstract us developers away from this sort of low-level 
code. So, let's do that same thing but let's do it the high-level Spring way. The `postResource()` method does the 
exact same things as the `exampleHandler()`. It reads the request body, converts that into a Resource object, stores 
that object in the list, then prepares the exact same response. Here is the method:

```Java 
@PostMapping("/resources")
@ResponseStatus(HttpStatus.ACCEPTED)
public @ResponseBody Resource postResource(@RequestBody Resource newResource) {
    this.resources.add(newResource);
    return newResource;
}
```

Note that we didn't even need to touch the request or response objects directly. Take a look at that method and see if 
you can answer the questions below.

## Exercises
The `postResource()` method does all the same things that the `exampleHandler()` method does. See if you can spot the 
syntax that told spring how to do the following for us:
1. How did a status code of 202 get set in the response?
2. How did we extract the request body in order to add it to the list?
3. How did we convert the object back into JSON for the response?