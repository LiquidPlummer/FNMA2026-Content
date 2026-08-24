package com.revature.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ExampleException2 extends RuntimeException {//this is an unchecked runtime exception
    public ExampleException2(String message) {
        super(message);
    }
}
