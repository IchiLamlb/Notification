package com.puneetchhabra.SMSConsumer.consumer;

import com.puneetchhabra.SMSConsumer.service.MessageHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static com.puneetchhabra.SMSConsumer.constants.Constants.GROUP_ID;
import static com.puneetchhabra.SMSConsumer.constants.Constants.TOPIC;

@Component
@Slf4j
public class PriorityAwarePartitionConsumer {
    private ConsumerFactory consumerFactory;
    private MessageHandlerService messageHandlerService;

    public PriorityAwarePartitionConsumer(ConsumerFactory consumerFactory, MessageHandlerService messageHandlerService){
        this.consumerFactory = consumerFactory;
        this.messageHandlerService = messageHandlerService;
    }

    @EventListener
    public void onAppStarted(ApplicationStartedEvent applicationStartedEvent){
         KafkaConsumer<String, String> consumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer(GROUP_ID,"email-consumer");

        TopicPartition PARTITION_PRIORITY_1 = new TopicPartition(TOPIC,0);
        TopicPartition PARTITION_PRIORITY_2 = new TopicPartition(TOPIC,1);
        TopicPartition PARTITION_PRIORITY_3 = new TopicPartition(TOPIC,2);

        // Assign partitions
        consumer.assign(Arrays.asList(
                PARTITION_PRIORITY_1,
                PARTITION_PRIORITY_2,
                PARTITION_PRIORITY_3
        ));

        while(true){
            try {
                // Fetch end offsets for all partitions
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Arrays.asList(
                        PARTITION_PRIORITY_1, PARTITION_PRIORITY_2, PARTITION_PRIORITY_3
                ));

                // Get current positions
                long positionP1 = consumer.position(PARTITION_PRIORITY_1);
                long positionP2 = consumer.position(PARTITION_PRIORITY_2);
                long positionP3 = consumer.position(PARTITION_PRIORITY_3);

                // Check offsets for priority-based pausing/resuming
                boolean hasPriority1Messages = (endOffsets.get(PARTITION_PRIORITY_1) - positionP1) > 0;

                if (hasPriority1Messages) {
                    // Pause lower-priority partitions
                    consumer.pause(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                } else {
                    boolean hasPriority2Messages = (endOffsets.get(PARTITION_PRIORITY_2) - positionP2) > 0;

                    if (hasPriority2Messages) {
                        // Pause the lowest-priority partition
                        consumer.pause(Arrays.asList(PARTITION_PRIORITY_3));
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2));
                    } else {
                        // Resume all partitions if no priority 1 or 2 messages are left
                        consumer.resume(Arrays.asList(PARTITION_PRIORITY_2, PARTITION_PRIORITY_3));
                    }
                }

                // Poll for new messages from active partitions
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(record -> processRecord(record)); // Process messages
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processRecord(ConsumerRecord record) {
        log.debug("Record Received: \n"+"Offset: " + record.offset()
                + ", Key: " + record.key() + ", Value: " + record.value());
        processMessage(record.value().toString());
    }

    private void processMessage(String message) {
        //process messages as if you are listening to fresh kafka topic
        //The above code will handle prioritization among partitions
        messageHandlerService.handleSmsRequest(message);
    }
}
