package com.coffee.sweepstakes.listener;

import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.event.SendEmailEvent;
import com.coffee.sweepstakes.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleSendEmailEvent(SendEmailEvent event) {
        User user = event.getUser();
        log.info("Received request to Send Email to userId: {} and Event: {}", user.getId(), event);
        emailService.sendEmail(user);
    }
}
