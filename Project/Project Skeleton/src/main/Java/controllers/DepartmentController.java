package controllers;

import io.javalin.http.Context;
import models.Department;
import services.DepartmentService;

public class DepartmentController {
    DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }


    //This is the method that would start the new dept workflow once we have a web API (wth Javalin)
    public void postDept(Context ctx) {
        //The API endpoint pattern in Javalin:
        //unwrap the request - get the parameters and body from the request
        //prep the work to be done - turn any params from req into params for service layer
        //call the service layer - invoke a SL method to start the workflow
        //return with the work done - we come back from the SL with results
        //prep the response - fill the response box and javalin will send it

        //unwrap the request - get the parameters and body from the request
        //This is a POST for the Department resource, so we need to grab the req body
        //prep the work to be done - turn any params from req into params for service layer
        //we turned the incoming JSON into an object
        Department dept = ctx.bodyAsClass(Department.class);


        //call the service layer - invoke a SL method to start the workflow
        Department result = departmentService.createDept(dept);



        //prep the response - fill the response box and javalin will send it
        ctx.json(result);
        ctx.status(201);
    }

    public void getDepartmentByName(Context ctx) {
        //The API endpoint pattern in Javalin:
        //unwrap the request - get the parameters and body from the request
        //need the name, how will I send this? as a path param, now we just need to get "name" from the URI
        //prep the work to be done - turn any params from req into params for service layer
        String name = ctx.pathParam("name");
        //call the service layer - invoke a SL method to start the workflow
        ctx.json(this.departmentService.findDepartmentByName(name));
        //return with the work done - we come back from the SL with results
        //prep the response - fill the response box and javalin will send it
    }

    public void getDepartments(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }

    public void updateDepartment(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }

    public void deleteDepartment(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }
}
