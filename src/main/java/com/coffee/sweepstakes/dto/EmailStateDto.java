package com.coffee.sweepstakes.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailStateDto {
    private String eventCode;
    private long totalUsers;
    private long emailSentCount;
    private long emailOpenedCount;
    private long facebookClickCount;
    private long instaClickCount;
    private long twitterClickCount;
    private long linkedinClickCount;
    private long newsRoomClickCount;
    private long uniqueClickCount;
}
