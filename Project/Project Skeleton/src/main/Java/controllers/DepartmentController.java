package controllers;

import models.Department;
import services.DepartmentService;

public class DepartmentController {
    DepartmentService departmentService;

    public void fakeMethod() {
        departmentService.fakeServiceMethod();
    }

    //This is the method that would start the new dept workflow once we have a web API (wth Javalin)
    public Department postDept() {
        return null;
    }
}
