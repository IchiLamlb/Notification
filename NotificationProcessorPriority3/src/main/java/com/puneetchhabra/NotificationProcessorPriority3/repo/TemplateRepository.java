package com.puneetchhabra.NotificationProcessorPriority3.repo;

import com.puneetchhabra.NotificationProcessorPriority3.models.db.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    // Find a template by name
    Optional<Template> findByName(String name);
}

