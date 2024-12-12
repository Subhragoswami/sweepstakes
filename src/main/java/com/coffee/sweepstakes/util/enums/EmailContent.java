package com.coffee.sweepstakes.util.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailContent {
    SUBJECT("Confirmation – New York Life’s Game Plan for Success Sweepstakes");
    private final String name;
}
