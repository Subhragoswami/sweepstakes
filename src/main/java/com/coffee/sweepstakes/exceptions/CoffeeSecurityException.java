package com.coffee.sweepstakes.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CoffeeSecurityException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;
}
