package com.revature.SPR_GCE_ERRORS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
public class MyController {

    private final ErrorService errorService;

    @Autowired
    public MyController(ErrorService errorService) {
        this.errorService = errorService;
    }

    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    public String generateException() throws Exception {
        this.errorService.throwException();
        return "This code is unreachable!";
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleSqlException(Exception exception) {
        return "A SQL Exception occurred!";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOtherExceptions(Exception exception) {
        return "One of the other exceptions occurred: " + exception.getMessage();
    }
}
