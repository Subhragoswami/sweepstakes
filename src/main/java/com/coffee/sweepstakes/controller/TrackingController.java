package com.coffee.sweepstakes.controller;

import com.coffee.sweepstakes.service.EmailTrackingService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TrackingController {
    private final EmailTrackingService emailTrackingService;

    @GetMapping("/tracking-image.png")
    public void trackEmailOpen(@RequestParam("eventCode") String eventCode, @RequestParam("email") String recipientEmail, HttpServletResponse response) {
        log.info("Tracking email open event for eventCode: {}, recipientEmail: {}", eventCode, recipientEmail);
        emailTrackingService.trackEmailOpen(eventCode,recipientEmail, response);
    }
    @GetMapping("/track-social-click/{platform}")
    public void trackSocialClick(@PathVariable String platform,@RequestParam("eventCode") String eventCode, @RequestParam("email") String recipientEmail, HttpServletResponse response) {
        log.info("Tracking social click event for platform: {}, eventCode: {}, recipientEmail: {}", platform, eventCode, recipientEmail);
        emailTrackingService.trackSocialClick(platform, eventCode, recipientEmail, response);
    }
}
