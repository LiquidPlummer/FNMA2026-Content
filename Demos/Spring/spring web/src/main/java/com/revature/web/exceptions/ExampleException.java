package com.revature.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
public class ExampleException extends RuntimeException {//this is an unchecked runtime exception
    public ExampleException(String message) {
        super("something different");
    }
}
