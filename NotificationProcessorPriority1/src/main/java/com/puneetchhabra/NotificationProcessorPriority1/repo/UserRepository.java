package com.puneetchhabra.NotificationProcessorPriority1.repo;

import com.puneetchhabra.NotificationProcessorPriority1.models.db.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}

