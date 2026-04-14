package com.puneetchhabra.NotificationProcessorPriority3.repo;

import com.puneetchhabra.NotificationProcessorPriority3.models.db.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}

