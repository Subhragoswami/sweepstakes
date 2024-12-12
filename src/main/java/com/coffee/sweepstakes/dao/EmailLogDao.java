package com.coffee.sweepstakes.dao;

import com.coffee.sweepstakes.entity.EmailLog;
import com.coffee.sweepstakes.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
@RequiredArgsConstructor
public class EmailLogDao {
    private final EmailLogRepository emailLogRepository;

    public EmailLog saveEmailLog(EmailLog emailLog) {
        return emailLogRepository.save(emailLog);
    }

    public Optional<EmailLog> getEmailLog(UUID userId) {
        return emailLogRepository.findByUserId(userId);
    }
}
