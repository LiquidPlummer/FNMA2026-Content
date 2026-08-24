package com.revature.web.aspects;


import com.revature.web.exceptions.ErrorResponse;
import com.revature.web.exceptions.ExampleException3;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(ExampleException3.class)
    //@ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ExampleException3 ex,
                                        HttpServletRequest request) {
        return new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage()
        );
    }


}
