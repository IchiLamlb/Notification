package com.ichilamlb.scheduler.service;

import com.ichilamlb.scheduler.models.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, NotificationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToNotificationTopic(NotificationRequest request) {
        try {
            Message<NotificationRequest> message = MessageBuilder
                    .withPayload(request)
                    .setHeader(KafkaHeaders.TOPIC, "priority-3")
                    .build();

            kafkaTemplate.send(message);
            log.debug("Sent NotificationRequest ID: {} to Kafka", request.getNotificationId());
        } catch (Exception e) {
            log.error("Error sending to Kafka: ", e);
        }
    }
}