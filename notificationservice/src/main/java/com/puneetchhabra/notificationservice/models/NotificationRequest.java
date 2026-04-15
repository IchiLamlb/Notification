package com.puneetchhabra.notificationservice.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private long notificationId;
    private String templateId;
    private Recipient recipient;
    private Content content;
    private int notificationPriority;
    private String idempotencyKey;

    // Đảm bảo có trường này và khởi tạo mặc định là mảng rỗng để tránh NPE
    private String[] channels = new String[0];
}