package com.ichilamlb.scheduler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Hỗ trợ Java 8 Date/Time (LocalDateTime) nếu cần
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}