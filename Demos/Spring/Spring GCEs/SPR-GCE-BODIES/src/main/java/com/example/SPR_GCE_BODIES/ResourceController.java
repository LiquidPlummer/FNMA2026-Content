package com.example.SPR_GCE_BODIES;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * The @RestController annotation is a combination of @Controller, the component scanning stereotype annotation,
 * and @ResponseBody. This annotation says that this class is a Controller bean, and it says that all of its
 * handler methods return RESTful response bodies if they return anything at all.
 */
@RestController
public class ResourceController {
    /**
     * This stuff is just setting up some example resources. This is not important to the functionality of a controller
     * class. You can ignore this whole first section.
     */
    Map<Integer, Resource> resources;
    public ResourceController() {
        this.resources = new HashMap<>();
        this.resources.put(0, new Resource(0, "Sam", "Gamgee", "samgardner@shire.co.uk"));
        this.resources.put(1, new Resource(1, "Frodo", "Baggins", "bagend@shire.co.uk"));
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Resource createResource(@RequestBody Resource newResource) {
        newResource.setResourceId(this.resources.size());
        this.resources.put(newResource.getResourceId(), newResource);
        return newResource;
    }

    @PutMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Resource updateResource(@RequestBody Resource resource, @PathVariable Integer resourceId) {
        this.resources.put(resourceId, resource);
        return this.resources.get(resourceId);
    }

    @GetMapping("/resources/{resourceId}")
    @ResponseStatus(HttpStatus.OK)
    public Resource getResource(@PathVariable Integer resourceId) {
        return this.resources.get(resourceId);
    }

    @GetMapping("/resources")
    @ResponseStatus(HttpStatus.OK)
    public Map<Integer, Resource> getResources() {
        return this.resources;
    }

}