package com.puneetchhabra.SMSConsumer.service.exceptions;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}

