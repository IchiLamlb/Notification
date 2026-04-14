package com.puneetchhabra.NotificationProcessorPriority1.service.exceptions;

import com.puneetchhabra.NotificationProcessorPriority1.models.db.Notification;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

