package com.example.SPR_GCE_REQMAPPING;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ResourceController {
    /**
     * This stuff is just setting up some example resources. This is not important to the functionality of a controller
     * class. You can ignore this whole first section.
     */
    List<Resource> resources;
    public ResourceController() {
        this.resources = new ArrayList<Resource>();
        this.resources.add(new Resource("Harry", "Potter", "protagonist@hogwarts.edu"));
        this.resources.add(new Resource("Luke", "Skywalker", "rogue.leader@rebelalliance.org"));
    }

    /**
     * In this request handler method we see how to work with the request and response objects, much like we would if
     * we were working in low-level Java servlets. If we were working in Javalin we would have a context object which
     * represents both the request and response objects. Spring offers us a multitude of ways to work, often providing
     * high- and low- level abstractions.
     *
     * We can work with the request and response objects if we want, but we can accomplish many common tasks without
     * having to touch them.
     */
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


    /**
     * This method does exactly the same thing as the exampleHandler() method above, but in far fewer lines of code.
     * All of the same things occur, but Spring takes care of most of the work. See if you can figure out what syntax
     * below tells Spring to do each of the following:
     * 1. Set a response status code of 202
     * 2. Extract the request body and covert the JSON into an object
     * 3. Convert the new object back into JSON and set it in the response body
     * (Hint: Spring loves annotations!)
     *
     * Also note the @PostMapping annotation. We don't need to specify the HTTP method like we did above, there are
     * special mapping annotations for GET, POST, PUT, PATCH, and DELETE methods.
     */
    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody Resource postResource(@RequestBody Resource newResource) {
        this.resources.add(newResource);
        return newResource;
    }


    /**
     * Similar to the method above, here we are able to code a simple GET response in a short handful of lines. We let
     * Spring handle the details. This takes the list of Resource objects, converts them all into JSON representations,
     * then sends that JSON back in the response body with a status of 200.
     */
    @GetMapping("/resources")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<Resource> getResources() {
        return this.resources;
    }


}
