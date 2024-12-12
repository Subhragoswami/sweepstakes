package com.coffee.sweepstakes.repository;

import com.coffee.sweepstakes.entity.SftpLogs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SftpLogsRepository extends JpaRepository<SftpLogs, UUID> {
}
