package com.curriculum.labs;

import org.springframework.stereotype.Service;

/**
 * Carried over from the previous lab, with a retry wrinkle — and two settings
 * hardcoded where configuration belongs. Part 1 fixes that.
 */
@Service
public class GreetingService {

    private final NotificationSender sender;

    // Part 1 externalizes these.
    private final String greeting = "Welcome aboard, %s!";
    private final int retryCount = 3;

    public GreetingService(NotificationSender sender) {
        this.sender = sender;
    }

    public void greet(String name) {
        String message = String.format(greeting, name);
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                sender.send(message);
                return;
            } catch (RuntimeException e) {
                System.out.println("attempt " + attempt + " failed: " + e.getMessage());
            }
        }
        System.out.println("giving up after " + retryCount + " attempts");
    }
}
