package com.revature.web.controllers;

import com.revature.web.models.ExampleModel;
import com.revature.web.services.ExampleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@RestController("ModelController")
@RequestMapping("/models")
public class ModelController {
    private ExampleService service;

    @Autowired
    public ModelController(ExampleService service) {
        System.out.println("Example Controller Constructor.");
        this.service = service;
    }


    //Not that the @ResponseBody annotation is already applied to this whole class
    //thanks to the @RestController annotation at the top, which implies both: Controller and ResponseBody
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ExampleModel getOneById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //Why do all this when we could just put @Responsebody on the method, it does the same thing
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new ExampleModel());
        resp.getWriter().write(json);
        resp.setStatus(200); //<- why do this, when spring gives us @ResponseStatus
        String contentLength = req.getHeader("Content-Length");
        return new ExampleModel(); // <-- this does the same thing thanks to the annotation
    }

    @GetMapping //?firstName=kyle&...
    public ExampleModel getAllWithFiltering(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName
    ) {
        //Now we can filter for any users who have that combo of first and last names
        return new ExampleModel();
    }

    @PostMapping
    public ExampleModel persist(@RequestBody ExampleModel model) {
        return this.service.saveOrUpdate(model);
    }

    @PutMapping
    public void putModel(
            @RequestHeader("Content-Length") String contentLength,
            @RequestHeader("Accept-Language") String language,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    ) {

    }

    @DeleteMapping("/{id}")
    public ExampleModel deleteModel(@PathVariable("id") Integer userId) {
        System.out.println(userId);
        return new ExampleModel();
    }

    //We can use the generic "RequestMapping" to do any of the types of request,
    // including ones that don't have a specific mapping annotation
    @RequestMapping(path = "/example", method=RequestMethod.OPTIONS)
    public String options() {
        return "This is how we might implement a mapping for something other than post, put, pathc, delte, get";
    }


}
