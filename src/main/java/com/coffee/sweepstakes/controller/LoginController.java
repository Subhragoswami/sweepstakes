package com.coffee.sweepstakes.controller;

import com.coffee.sweepstakes.service.LoginService;
import com.coffee.sweepstakes.model.request.LoginRequest;
import com.coffee.sweepstakes.model.response.LoginResponse;
import com.coffee.sweepstakes.model.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "Login API")
    public ResponseDto<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Received request for login {}", loginRequest);
        return loginService.login(loginRequest);
    }

}
