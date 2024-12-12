package com.coffee.sweepstakes.validator;

import com.coffee.sweepstakes.dao.UserDao;
import com.coffee.sweepstakes.model.request.UserRequest;
import com.coffee.sweepstakes.util.ErrorConstants;
import com.coffee.sweepstakes.exceptions.ValidationException;
import com.coffee.sweepstakes.model.response.ErrorDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\(?([0-9]{3})\\)?[-.●]?([0-9]{3})[-.●]?([0-9]{4})$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private final UserDao userDao;

    public void requestValidator(UserRequest userRequest) {
        List<ErrorDto> errorDtoList = new ArrayList<>();
        if (ObjectUtils.isEmpty(userRequest)) {
            errorDtoList.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "request body can't be empty")));
        }
        if (ObjectUtils.isEmpty(userRequest.getFirstName())) {
            errorDtoList.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "firstName")));
        }
        if (ObjectUtils.isNotEmpty(userRequest.getUserPhoneNumber()) && !PHONE_NUMBER_PATTERN.matcher(userRequest.getUserPhoneNumber()).matches()) {
            errorDtoList.add(new ErrorDto(ErrorConstants.INVALID_ERROR_CODE, MessageFormat.format(ErrorConstants.INVALID_ERROR_CODE_MESSAGE, "phone number")));
        }
        if (ObjectUtils.isEmpty(userRequest.getLastName())) {
            errorDtoList.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "lastName")));
        }
        if (ObjectUtils.isEmpty(userRequest.getUserEmail())) {
            errorDtoList.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "email")));
        }
        if (userRequest.isConsentToContact() && ObjectUtils.isEmpty(userRequest.getUserPhoneNumber())) {
            errorDtoList.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "phone number")));
        }
        if (ObjectUtils.isNotEmpty(userRequest.getUserPhoneNumber()) && !EMAIL_PATTERN.matcher(userRequest.getUserEmail()).matches()) {
            errorDtoList.add(new ErrorDto(ErrorConstants.INVALID_ERROR_CODE, MessageFormat.format(ErrorConstants.INVALID_ERROR_CODE_MESSAGE, "email")));
        }
        if (ObjectUtils.isEmpty(userRequest.getEventCode())) {
            errorDtoList.add(new ErrorDto(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "eventCode")));
        }

        if (userDao.existsByUserEmail(userRequest.getUserEmail(), userRequest.getEventCode())) {
            errorDtoList.add(new ErrorDto(ErrorConstants.ALREADY_PRESENT_ERROR_CODE, MessageFormat.format(ErrorConstants.ALREADY_PRESENT_ERROR_MESSAGE, "duplicate email ", "email")));
        }
        if (CollectionUtils.isNotEmpty(errorDtoList)) {
            throw new ValidationException(errorDtoList);
        }
    }
}
