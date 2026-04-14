package com.puneetchhabra.SMSConsumer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendSmsResponse {
    private int status;
    private String message;
}
