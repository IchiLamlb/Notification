package com.puneetchhabra.PushNConsumer.service.exceptions;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}

