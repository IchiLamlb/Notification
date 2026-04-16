package com.ichilamlb.EmailConsumer.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerValidationResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Thêm builder để dễ khởi tạo trong Service
public class SendEmailResponse {
    private int status;
    private String message;
}
