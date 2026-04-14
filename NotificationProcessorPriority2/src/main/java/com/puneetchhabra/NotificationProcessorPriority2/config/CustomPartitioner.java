package com.puneetchhabra.NotificationProcessorPriority2.config;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

public class CustomPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        return 1;
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
