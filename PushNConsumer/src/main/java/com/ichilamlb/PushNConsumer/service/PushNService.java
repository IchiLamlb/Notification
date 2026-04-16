package com.ichilamlb.PushNConsumer.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.ichilamlb.PushNConsumer.models.PushNRequest;
import com.ichilamlb.PushNConsumer.models.SendPushNResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNService {

    public SendPushNResponse sendPushNotification(PushNRequest pushNRequest) {
        try {
            // 1. Tạo đối tượng Notification (Tiêu đề và Nội dung)
            Notification notification = Notification.builder()
                    .setTitle(pushNRequest.getTitle()) // Lấy từ request
                    .setBody(pushNRequest.getBody())   // Lấy từ request
                    .build();

            // 2. Xây dựng Message gửi tới FCM dựa trên Token của thiết bị
            Message message = Message.builder()
                    .setToken(pushNRequest.getRecipientToken()) // FCM Token của máy ảo Android
                    .setNotification(notification)
                    .build();

            // 3. Thực hiện gửi
            String response = FirebaseMessaging.getInstance().send(message);

            log.info("Successfully sent push message: {}", response);
            return new SendPushNResponse(200, "Push notification sent successfully. FCM ID: " + response);

        } catch (Exception e) {
            log.error("Error sending push notification to token: {}", pushNRequest.getRecipientToken(), e);
            return new SendPushNResponse(500, "Failed to send push: " + e.getMessage());
        }
    }
}