package com.puneetchhabra.notificationservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class PushNotification {
    private String title;
    private Action action;
}
