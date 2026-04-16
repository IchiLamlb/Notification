package com.ichilamlb.NotificationProcessorPriority3.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {
    // BỔ SUNG: ID để theo dõi thông báo xuyên suốt hệ thống
    private long notificationId;

    // BỔ SUNG: Key chống gửi trùng (Nếu cần dùng trong logic xử lý)
    private String idempotencyKey;

    private int notificationPriority;
    private String[] channels;
    private Recipient recipient;
    private Content content;
}
