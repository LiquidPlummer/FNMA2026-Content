package com.curriculum.labs;

import org.springframework.stereotype.Component;

/**
 * The "real" sender. Pretend send() costs money and emails actual customers —
 * which is why Part 4 makes sure it only exists in production.
 */
@Component
public class EmailSender implements NotificationSender {

    @Override
    public void send(String message) {
        System.out.println("[email] " + message + "   (imagine a real SMTP call here)");
    }
}
