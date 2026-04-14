package com.puneetchhabra.NotificationProcessorPriority3.repo;

import com.puneetchhabra.NotificationProcessorPriority3.models.db.Preference;
import com.puneetchhabra.NotificationProcessorPriority3.models.enums.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    // Find preferences for a specific user
    List<Preference> findByUserId(Long userId);

    // Find preferences for a user and specific channel
    Optional<Preference> findByUserIdAndChannel(Long userId, Channel channel);
}

