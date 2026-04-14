package com.puneetchhabra.notificationservice.controllers;

import com.puneetchhabra.notificationservice.models.NotificationRequest;
import com.puneetchhabra.notificationservice.service.KafkaService;
import com.puneetchhabra.notificationservice.service.NotificationProcessingService;
import com.puneetchhabra.notificationservice.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api")
public class NotificationsController {

    KafkaService kafkaService;
    RedisService redisService;
    NotificationProcessingService notificationProcessingService;

    public NotificationsController(KafkaService kafkaService, RedisService redisService, NotificationProcessingService notificationProcessingService){
        this.kafkaService = kafkaService;
        this.redisService = redisService;
        this.notificationProcessingService = notificationProcessingService;
    }

    @GetMapping("/health")
    public String getHealth(){
        return "Running";
    }

    @PostMapping("/send-notification")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest notificationRequest){
        try {
            //checks for valid request
            notificationProcessingService.validateRequest(notificationRequest);

            //if notification priority given, simply forward to kafka
            //else assign priority, then forward to kafka
            //-1 priority means no priority, need to assign priority
            if (notificationRequest.getNotificationPriority() == -1) {
                notificationProcessingService.assignPriority(notificationRequest);
            }

            kafkaService.sendNotification(notificationRequest);
            log.debug("Notification forwarded to Kafka Service with priority: "+notificationRequest.getNotificationPriority());
            return ResponseEntity.accepted().body("Notification accepted for processing.");
        } catch (InvalidRequestException e){
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (ResponseStatusException e){
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (KafkaException e) {
            log.error("Failed to forward notification to Kafka: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing notification.");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
