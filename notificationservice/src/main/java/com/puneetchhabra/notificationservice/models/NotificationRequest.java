package com.puneetchhabra.notificationservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private int notificationPriority;
    private String[] channels;
    private Recipient recipient;
    private Content content;
}
