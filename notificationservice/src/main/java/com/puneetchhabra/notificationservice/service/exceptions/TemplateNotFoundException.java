package com.puneetchhabra.notificationservice.service.exceptions;

public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String message) {
        super(message);
    }
}

