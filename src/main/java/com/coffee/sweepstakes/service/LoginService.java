package com.coffee.sweepstakes.service;

import com.coffee.sweepstakes.config.ServiceConfig;
import com.coffee.sweepstakes.model.request.LoginRequest;
import com.coffee.sweepstakes.model.response.LoginResponse;
import com.coffee.sweepstakes.model.response.ResponseDto;
import com.coffee.sweepstakes.security.AuthenticationService;
import com.coffee.sweepstakes.validator.LoginValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {
    private final ServiceConfig serviceConfig;

    private final AuthenticationService authenticationService;

    public ResponseDto<LoginResponse> login(LoginRequest requestBody) {
        LoginValidator.requestValidation(requestBody, serviceConfig);
        LoginResponse loginResponse = new LoginResponse(authenticationService.signIn(requestBody));
        return ResponseDto.<LoginResponse>builder().status(0).data(List.of(loginResponse)).build();
    }
}


