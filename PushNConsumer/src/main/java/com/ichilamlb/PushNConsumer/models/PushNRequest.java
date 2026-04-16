package com.ichilamlb.PushNConsumer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushNRequest {
    // Trường này cực kỳ quan trọng để xử lý DB trong PushNProcessingService
    private Long notificationId;

    // Token thiết bị Android (lấy từ máy ảo)
    private String recipientToken;

    // Các trường dữ liệu phục vụ hiển thị trên điện thoại
    private String title;
    private String body;
}