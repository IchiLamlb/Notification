package com.ichilamlb.scheduler.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Thêm builder để dễ khởi tạo trong Service
public class SendEmailResponse {
    private int status;
    private String message;
}
