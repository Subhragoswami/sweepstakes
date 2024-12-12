package com.coffee.sweepstakes.controller;

import com.coffee.sweepstakes.service.SFTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/sftp")
@RequiredArgsConstructor
public class SFTPController {

    private final SFTPService sftpService;

    @PostMapping("/upload/{eventCode}")
    public ResponseEntity<String> uploadFileToSFTP(@PathVariable String eventCode) {
        log.info("Received request to upload file to SFTP for event code {}", eventCode);
        sftpService.uploadFileToSFTP(eventCode);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @PostMapping("/download")
    public ResponseEntity downloadFileFromSFTP(@RequestParam(name = "fileName") String fileName) {
        log.info("Received request to download {} file from SFTP", fileName);
        return sftpService.downloadFile(fileName);
    }

    @PostMapping("/deleteFile")
    public ResponseEntity<String> deleteFileFromSFTP(@RequestParam(name = "fileName") String fileName) {
        log.info("Received request to delete {} file from SFTP", fileName);
        return sftpService.deleteFileFromSFTP(fileName);
    }

    @PostMapping("/email-campaigns")
    public ResponseEntity<String> uploadEmailCampaignToSFTP() {
        log.info("Received Request for generate email campaign csv");
        sftpService.uploadEmailCampaignToSFTP();
        return ResponseEntity.ok("File uploaded successfully");
    }

    @PostMapping("/summary-report/{eventCode}")
    public ResponseEntity<String> uploadSummaryFileToSFTP(@PathVariable String eventCode) {
        log.info("Request received for create summary file.");
        sftpService.uploadSummaryFileToSFTP(eventCode);
        return ResponseEntity.ok("File uploaded successfully");
    }
}
