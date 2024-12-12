package com.coffee.sweepstakes.model.response;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class EventResponse {
    private UUID id;
    private String eventName;
    private String zone;
    private String generalOffice;
    private String eventLocation;
    private Date eventStartDate;
    private Date eventEndDate;
    private String eventCode;
    private boolean isActive;
    private boolean isRegistrationActive;
    private Date createdAt;
    private Date updatedAt;
}
