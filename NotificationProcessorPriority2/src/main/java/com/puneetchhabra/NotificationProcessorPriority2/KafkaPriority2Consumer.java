package com.puneetchhabra.NotificationProcessorPriority2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puneetchhabra.NotificationProcessorPriority2.models.NotificationRequest;
import com.puneetchhabra.NotificationProcessorPriority2.service.NotificationProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.puneetchhabra.NotificationProcessorPriority2.constants.Constants.TOPIC_PRIORITY_2;

@Component
@Slf4j
public class KafkaPriority2Consumer {
    NotificationProcessingService notificationProcessingService;
    public KafkaPriority2Consumer(NotificationProcessingService notificationProcessingService){
        this.notificationProcessingService = notificationProcessingService;
    }
    @KafkaListener(topics = TOPIC_PRIORITY_2)
    public void consumeNotificationRequest(String notificationRequestString){
        ObjectMapper mapper = new ObjectMapper();

        try{
            JsonNode notificationRequestJson = mapper.readTree(notificationRequestString);
            NotificationRequest notificationRequest = mapper.treeToValue(notificationRequestJson,NotificationRequest.class);
            log.debug("Successfully parsed Consumed Notification Request: {}", notificationRequest.toString());
            try{
                notificationProcessingService.processNotification(notificationRequest);
            } catch (Exception exception){
                log.error("Unexpected Exception in NotificationProcessingService while processing Notification Request: {}", notificationRequest);
                log.error("Exception: {}", exception.toString());
            }
        } catch (JsonProcessingException jsonProcessingException){
            log.error("Error parsing kafka consumed message to JSON. Exception: \n {}", jsonProcessingException.toString());
        }
    }
}
