package com.coffee.sweepstakes.model.request;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
public class UserRequest {
    private String firstName;
    private String lastName;
    private String userEmail;
    private String userPhoneNumber;
    private String eventCode;
    private boolean consentToContact;
    private boolean consentMarketing;
    @Builder.Default
    private boolean isActive = true;
}
