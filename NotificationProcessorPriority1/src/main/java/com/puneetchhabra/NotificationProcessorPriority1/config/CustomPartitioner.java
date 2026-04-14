package com.puneetchhabra.NotificationProcessorPriority1.config;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

public class CustomPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        return 0;
    }

    @Override
    public void close() {
        // Perform any necessary cleanup
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // Perform any necessary configuration
    }
}
