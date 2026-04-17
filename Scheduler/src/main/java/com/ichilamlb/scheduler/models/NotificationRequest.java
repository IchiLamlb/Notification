package com.ichilamlb.scheduler.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ichilamlb.scheduler.models.Recipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {
    private int notificationId;
    private int notificationPriority;
    private List<String> channels;
    private Recipient recipient;
    private Content content;
}