package com.coffee.sweepstakes.repository;

import com.coffee.sweepstakes.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
    Optional<EmailLog> findByUserId(UUID userId);
}
