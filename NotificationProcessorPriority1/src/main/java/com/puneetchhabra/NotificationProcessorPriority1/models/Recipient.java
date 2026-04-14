package com.puneetchhabra.NotificationProcessorPriority1.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class Recipient {
    private String userId;
    private String userEmail;

}
