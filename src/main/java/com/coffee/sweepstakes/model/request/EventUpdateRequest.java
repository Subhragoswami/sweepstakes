package com.coffee.sweepstakes.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventUpdateRequest {
    private String eventName;
    private String eventLocation;
    private Date eventStartDate;
    private Date eventEndDate;
    private Boolean isActive;
}
