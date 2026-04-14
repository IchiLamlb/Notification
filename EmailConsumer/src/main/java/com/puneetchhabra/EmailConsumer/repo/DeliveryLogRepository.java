package com.puneetchhabra.EmailConsumer.repo;


import com.puneetchhabra.EmailConsumer.models.db.DeliveryLog;
import com.puneetchhabra.EmailConsumer.models.enums.Channel;
import com.puneetchhabra.EmailConsumer.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
    // Find delivery logs for a specific notification
    List<DeliveryLog> findByNotificationId(Long notificationId);

    // Find delivery logs by channel
    List<DeliveryLog> findByChannel(Channel channel);

    // Find delivery logs by status
    List<DeliveryLog> findByStatus(Status status);
}

