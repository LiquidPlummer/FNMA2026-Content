package com.example.SPR_GCE_BODIES;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class ExampleController {
    /**
     * We can read the body of any request by getting the request object, getting a reader for the body, and using that
     * to read out the contents. In this method Spring passes us the request object, which contains the body. We
     * use the method .getReader() to get a BufferedReader object which we can use to read the body contents as long
     * as there is still information to read.
     */
    @PostMapping("/readRequestBody")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void readRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader bufferedReader = request.getReader();
        while(bufferedReader.ready()) {
            System.out.println(bufferedReader.readLine());
        }
    }

    /**
     * Similar to reading a request body from the HttpServletRequest object above, we can write to the response object.
     * We once again ask Spring to pass us a parameter, this time we want the response object instead of the request.
     * From there we use .getWriter() to get a PrintWriter object which we can use to write  information to the response
     * body. After that, we're done. The response now contains the body, and Spring will send it without us having to
     * take any further action.
     */
    @GetMapping("/writeResponseBody")
    @ResponseStatus(HttpStatus.OK)
    public void writeResponseBody(HttpServletResponse response) throws IOException {
        String responseText = """
                {
                    "firstName":"first",
                    "lastName":"last",
                    "email":"test@test.com"
                }""";

        PrintWriter printWriter = response.getWriter();
        printWriter.write(responseText);
    }

    /**
     * Here we don't even bother with the PrintWriter. We give this method a return type of String, which Spring
     * will understand to mean that we are returning text which should be sent back as the response body. We don't need
     * to even bother with the rest of the response, Spring will handle the rest.
     */
    @GetMapping("/returnResponseBody")
    @ResponseStatus(HttpStatus.OK)
    public String returnResponseBody() {
        return """
                {
                    "firstName":"first",
                    "lastName":"last",
                    "email":"test@test.com"
                }""";
    }

    /**
     * Spring Web is designed with REST in mind. We can read a request body and convert the resource representation
     * there into an object by using the @RequestBody annotation.
     */
    @PostMapping("/restfulRequestBody")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void restfulRequestBody(@RequestBody Resource newResource) {
        System.out.println(newResource);
    }

    /**
     * Similarly, we can use the @ResponseBody annotation to tell Spring to convert an object into a resource
     * representation and write that into the response body.
     */
    @GetMapping("/restfulResponseBody")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Resource restfulResponseBody() {
        return new Resource("first", "last", "test@test.com");
    }

    /**
     * We can also use the ResponseEntity class to build up a response. In this example we use the static method `.ok()`
     * and we pass the object. There are a few ways to write the body with ResponseEntity, in this case `.ok()`
     * indicates the response should have a status code of 200 and may include a resource representation which is
     * converted to JSON from the object passed as a parameter.
     */
    @GetMapping("/responseEntity")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> responseEntity() {
        return ResponseEntity.ok(new Resource("first", "last", "test@test.com"));
    }
}
