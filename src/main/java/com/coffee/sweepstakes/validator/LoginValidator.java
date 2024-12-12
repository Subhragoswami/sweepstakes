package com.coffee.sweepstakes.validator;

import com.coffee.sweepstakes.config.ServiceConfig;
import com.coffee.sweepstakes.exceptions.ValidationException;
import com.coffee.sweepstakes.model.request.LoginRequest;
import com.coffee.sweepstakes.model.response.ErrorDto;
import com.coffee.sweepstakes.util.ErrorConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginValidator {
    public static void requestValidation(LoginRequest request, ServiceConfig serviceConfig) {
        List<ErrorDto> errorMessages = new ArrayList<>();

        if (StringUtils.isEmpty(request.getUserName())) {
            errorMessages.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "userName")));
        }
        if (StringUtils.isEmpty(request.getPassword())) {
            errorMessages.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "password")));
        } else if (!request.getUserName().equals(serviceConfig.getUserName())){
            errorMessages.add(new ErrorDto(ErrorConstants.INVALID_ERROR_CODE, MessageFormat.format(ErrorConstants.INVALID_ERROR_CODE_MESSAGE, "userName")));
        }
        else if (!request.getPassword().equals(serviceConfig.getPassword())){
            errorMessages.add(new ErrorDto(ErrorConstants.INVALID_ERROR_CODE, MessageFormat.format(ErrorConstants.INVALID_ERROR_CODE_MESSAGE, "password")));
        }
        if (CollectionUtils.isNotEmpty(errorMessages)) {
            throw new ValidationException(errorMessages);
        }
    }
}