package com.puneetchhabra.PushNConsumer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushNRequest {
    private String title;
    private String message;
    private String action;
    private Long notificationId;

    public PushNRequest(String title, String message, String action){
        this.title = title;
        this.message = message;
        this.action = action;
    }
}
