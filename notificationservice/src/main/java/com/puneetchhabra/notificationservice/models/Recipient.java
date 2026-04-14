package com.puneetchhabra.notificationservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor @NoArgsConstructor
public class Recipient {
    private String userId;
    private String userEmail;

}
