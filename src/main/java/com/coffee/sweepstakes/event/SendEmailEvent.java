package com.coffee.sweepstakes.event;

import com.coffee.sweepstakes.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SendEmailEvent extends ApplicationEvent {
    private final User user;

    public SendEmailEvent (Object source, User user) {
        super(source);
        this.user = user;
    }
}
