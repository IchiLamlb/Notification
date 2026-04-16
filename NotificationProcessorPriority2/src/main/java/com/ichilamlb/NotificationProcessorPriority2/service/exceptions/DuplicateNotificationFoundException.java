package com.ichilamlb.NotificationProcessorPriority2.service.exceptions;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

