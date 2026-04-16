package com.ichilamlb.EmailConsumer.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    private String emailId;          // Đây là địa chỉ email người nhận
    private String message;          // Nội dung email (body)
    private String emailSubject;     // Tiêu đề email
    private String[] emailAttachments;
    private Long notificationId;

    // Constructor tùy chỉnh bạn đã viết (giữ lại để không lỗi các chỗ cũ)
    public EmailRequest(String emailId, String message, String emailSubject, String[] emailAttachments){
        this.emailId = emailId;
        this.message = message;
        this.emailSubject = emailSubject;
        this.emailAttachments = emailAttachments;
    }
}