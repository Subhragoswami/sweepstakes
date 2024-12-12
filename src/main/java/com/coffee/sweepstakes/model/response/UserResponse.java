package com.coffee.sweepstakes.model.response;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String userEmail;
    private String userPhoneNumber;
    private boolean isActive;
    private String eventCode;
    private boolean consentToContact;
    private boolean consentMarketing;
    private Date createdAt;
    private Date updatedAt;
    private boolean isEmailSent;
    private boolean isEmailOpened;
    private boolean isFacebookLinkClicked;
    private boolean isInstaLinkClicked;
    private boolean isTwitterLinkClicked;
    private boolean isLinkedInLinkClicked;
    private boolean isNewsRoomClicked;
    private String zone;
    private String generalOffice;
    private String eventName;
    private Date eventStartDate;
}
