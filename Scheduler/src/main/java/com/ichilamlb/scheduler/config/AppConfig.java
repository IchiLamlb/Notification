package com.ichilamlb.scheduler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /**
     * Khởi tạo và cung cấp ObjectMapper Bean cho toàn bộ hệ thống (Application Context)
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}