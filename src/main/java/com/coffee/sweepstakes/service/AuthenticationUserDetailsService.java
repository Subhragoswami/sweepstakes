package com.coffee.sweepstakes.service;


import com.coffee.sweepstakes.config.ServiceConfig;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.model.User;
import com.coffee.sweepstakes.util.ErrorConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class AuthenticationUserDetailsService {
    private final ServiceConfig serviceConfig;

    public UserDetailsService userDetailsService() {
        return username -> getUser().orElseThrow
                (() -> new CoffeeException(ErrorConstants.LOGIN_USER_NOT_FOUND_ERROR_CODE, ErrorConstants.LOGIN_USER_NOT_FOUND_ERROR_MESSAGE));

    }

    public Optional<User> getUser() {
        User user = new User();
        user.setUserName(serviceConfig.getUserName());
        user.setPassword(serviceConfig.getPassword());
        return Optional.of(user);
    }
}
