package com.puneetchhabra.PushNConsumer.repo;

import com.puneetchhabra.PushNConsumer.models.db.Notification;
import com.puneetchhabra.PushNConsumer.models.enums.Channel;
import com.puneetchhabra.PushNConsumer.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find notifications for a specific user
    List<Notification> findByUserId(Long userId);

    // Find notifications by user and channel
    List<Notification> findByUserIdAndChannel(Long userId, Channel channel);

    // Find notifications by status
    List<Notification> findByStatus(Status status);
}

