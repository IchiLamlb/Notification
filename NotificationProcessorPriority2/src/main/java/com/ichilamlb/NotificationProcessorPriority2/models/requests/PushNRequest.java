package com.ichilamlb.NotificationProcessorPriority2.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushNRequest {
    private String title;
    private String body;           // THÊM TRƯỜNG NÀY
    private String actionUrl;
    private Long notificationId;   // THÊM TRƯỜNG NÀY
    private String recipientToken; // THÊM TRƯỜNG NÀY

    // Constructor cũ nếu bạn muốn giữ tương thích (tùy chọn)
    public PushNRequest(String title, String body, String actionUrl) {
        this.title = title;
        this.body = body;
        this.actionUrl = actionUrl;
    }
}
