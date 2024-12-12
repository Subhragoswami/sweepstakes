package com.coffee.sweepstakes.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CoffeeException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

}

