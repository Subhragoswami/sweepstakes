package com.coffee.sweepstakes.model.request;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
public class EventFilterRequest {
    private String eventName;
}
