package com.ichilamlb.scheduler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendMessage(String topic, Object payload) {
        try {
            // Chuyển Object thành chuỗi JSON thuần túy
            String jsonString = objectMapper.writeValueAsString(payload);

            // Gửi vào Kafka
            kafkaTemplate.send(topic, jsonString);

            logger.info("Đã đẩy tin vào Topic [{}]: {}", topic, jsonString);
        } catch (JsonProcessingException e) {
            logger.error("Lỗi parse JSON khi gửi tới Kafka: {}", e.getMessage());
        }
    }
}