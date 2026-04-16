package com.ichilamlb.NotificationProcessorPriority1.service.exceptions;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

