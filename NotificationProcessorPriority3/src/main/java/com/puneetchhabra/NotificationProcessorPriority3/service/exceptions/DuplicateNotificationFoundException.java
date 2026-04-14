package com.puneetchhabra.NotificationProcessorPriority3.service.exceptions;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

