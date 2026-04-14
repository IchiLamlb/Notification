package com.puneetchhabra.NotificationProcessorPriority2.service.exceptions;

import com.puneetchhabra.NotificationProcessorPriority2.models.db.Notification;

public class DuplicateNotificationFoundException extends RuntimeException {
    public DuplicateNotificationFoundException(String message) {
        super(message);
    }
}

