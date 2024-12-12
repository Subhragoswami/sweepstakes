package com.coffee.sweepstakes.dao;

import com.coffee.sweepstakes.entity.SftpLogs;
import com.coffee.sweepstakes.repository.SftpLogsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SftpLogsDao {

    private final SftpLogsRepository sftpLogsRepository;

    public SftpLogs saveSftpLogs(SftpLogs sftpLogs) {
        return sftpLogsRepository.save(sftpLogs);
    }
}
