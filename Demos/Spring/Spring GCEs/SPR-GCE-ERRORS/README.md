# SPR-GCE-ERRORS
In this example we will explore exception handling in Spring Web.

If you clone this project and want to run it yourself, remember to get the maven dependencies after cloning with the CLI command: `mvn install`.

### Version Information
| Software       | Version |
|----------------|---------|
| SpringBoot     | 3.3.5   |
| Spring Web     | 6.1.14  |
| Java           | 21      |

## Web Request Handlers
In Spring Web we have controllers, which are beans with request handler methods. Recall that these
methods are registered with Spring and mapped to URL paths. When a request comes in, our Spring Web
server "listens" for it, then invokes the appropriate handler based on URL. From there our server processes
the request until it is ready with a response. If an exception is thrown all the way back
up to and out of the request handler, Spring will then invoke the appropriate exception handler method.

## Exception Handlers
Exception handlers are very similar to request handlers. If a request handler produces a response when the server
successfully handles a request, an exception handler's job is to produce a response if the server was not successful. 
 - The event which triggers a request handler is an incoming request
 - The event which triggers an exception handler is an exception being thrown from a request handler

Take a look at the MyController class. You will see a single request handler:
```Java
    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    public String generateException() throws Exception {
        this.errorService.throwException();
        return "This code is unreachable!";
    }
```

And two exception handlers:
```Java
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleSqlException(Exception exception) {
        return "A SQL Exception occurred!";
    }
```
*`handleSqlException()` is invoked if a `SQLException` is thrown*

```Java
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOtherExceptions(Exception exception) {
        return "One of the other exceptions occurred: " + exception.getMessage();
    }
```
*`handleOtherExceptions()` is invoked if a any other type of exception is thrown.*

Be sure to note how we changed the response status code when an exception occurs. A successful response would send back
code 200 'OK'. If we generate an exception one of the two exception handlers will take over, and instead we will 
get status code 400 'Bad Request' or 500 'Server Error' depending on which.

The `@ResponseStatus` annotation is used here to set status codes in the response, but we could also use other 
techniques like bringing in `HttpServletResponse` or `ResponseEntity` objects. Exception handlers generally work 
the same way as request handlers.

## Specificity
Unlike try-catch where we must order the catch blocks to make sure we properly handle specific types of exception, 
here we don't need to worry about that. The order in which we write methods does not affect how exceptions are handled. 
Spring will be as specific as possible. Try it out with this example application, create a get request to the '/test' 
endpoint and send it a few times. We should see that whenever a `SQLException` is thrown we get a slightly different 
message because it was handled by a different handler. 