package com.revature.logging.components;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessagePrinter {
    @Value("${app.message}")
    private String message;

    @PostConstruct
    public void print() {
        System.out.println(message);
    }
}
