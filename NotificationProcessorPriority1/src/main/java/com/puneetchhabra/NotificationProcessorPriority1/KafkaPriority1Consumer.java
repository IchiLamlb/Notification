package com.puneetchhabra.NotificationProcessorPriority1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puneetchhabra.NotificationProcessorPriority1.models.NotificationRequest;
import com.puneetchhabra.NotificationProcessorPriority1.service.NotificationProcessingService;
import com.puneetchhabra.NotificationProcessorPriority1.service.exceptions.DuplicateNotificationFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.puneetchhabra.NotificationProcessorPriority1.constants.Constants.TOPIC_PRIORITY_1;

@Component
@Slf4j
public class KafkaPriority1Consumer {
    NotificationProcessingService notificationProcessingService;
    ObjectMapper mapper;
    public KafkaPriority1Consumer(NotificationProcessingService notificationProcessingService, ObjectMapper mapper){
        this.notificationProcessingService = notificationProcessingService;
        this.mapper = mapper;
    }
    @KafkaListener(topics = TOPIC_PRIORITY_1)
    public void consumeNotificationRequest(String notificationRequestString){

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
