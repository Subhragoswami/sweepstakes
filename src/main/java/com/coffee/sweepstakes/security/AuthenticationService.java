package com.coffee.sweepstakes.security;

import com.coffee.sweepstakes.util.ErrorConstants;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.model.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private int tokenExpiryTimeHr;

    @Value("${token.expiry.time.hr}")
    public void setTokenExpiryTimeHr(int tokenExpiryTimeHr) {
        this.tokenExpiryTimeHr = tokenExpiryTimeHr;
    }

    public String signIn(LoginRequest loginRequestData) {
        Authentication authentication = authentication(loginRequestData);
        Date expiryTime = DateUtils.addHours(new Date(), tokenExpiryTimeHr);
        String jwtToken = createJWTToken(authentication, loginRequestData, expiryTime);
        log.debug("jwtToken {}", jwtToken);
        return jwtToken;
    }


    private Authentication authentication(LoginRequest authenticatedUser) {
        if (ObjectUtils.isEmpty(authenticatedUser)) {
            throw new CoffeeException(ErrorConstants.LOGIN_USER_NOT_FOUND_ERROR_CODE, ErrorConstants.LOGIN_USER_NOT_FOUND_ERROR_MESSAGE);
        }
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticatedUser.getUserName(), authenticatedUser.getPassword()));
        log.debug("authenticate {}", authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private String createJWTToken(Authentication authentication, LoginRequest authenticatedUser, Date expiryTime) {
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        return jwtService.generateToken(authenticatedUser, expiryTime, scope);
    }
}
