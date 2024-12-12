package com.coffee.sweepstakes.service;

import com.coffee.sweepstakes.dao.EventDao;
import com.coffee.sweepstakes.dao.UserDao;
import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.entity.Event;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.model.request.UserRequest;
import com.coffee.sweepstakes.model.response.EventResponse;
import com.coffee.sweepstakes.util.ErrorConstants;
import com.coffee.sweepstakes.util.SFTPFileUtils;
import com.coffee.sweepstakes.validator.UserValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.coffee.sweepstakes.model.response.ResponseDto;
import com.coffee.sweepstakes.model.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.coffee.sweepstakes.event.SendEmailEvent;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

import static com.coffee.sweepstakes.util.AppConstants.RESPONSE_SUCCESS;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final EventDao eventDao;
    private final UserValidator userValidator;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EventService eventService;

    public ResponseDto<User> saveUser(UserRequest userRequest) {
        userValidator.requestValidator(userRequest);
        User user = mapper.convertValue(userRequest, User.class);
        user = userDao.saveUser(user);
        eventPublisher.publishEvent(new SendEmailEvent(this, user));
        return ResponseDto.<User>builder()
                .status(RESPONSE_SUCCESS)
                .data(List.of(user))
                .build();
    }
    public ResponseDto<UserResponse> getById(UUID id) {
        User user = userDao.getUserById(id)
                .orElseThrow(() -> new CoffeeException(ErrorConstants.NOT_FOUND_ERROR_CODE, MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "User")));
        UserResponse userResponse = mapper.convertValue(user, UserResponse.class);
        return ResponseDto.<UserResponse>builder()
                .status(RESPONSE_SUCCESS)
                .data(List.of(userResponse))
                .build();
    }

    public ResponseDto<UserResponse> getAllUsers(Pageable pageable, String search, String eventCode) {
        Page<User> users;
        users = ObjectUtils.isEmpty(search)
                ? (ObjectUtils.isEmpty(eventCode) ? userDao.getAllUsers(pageable) : userDao.getUsersByEventCode(pageable, eventCode))
                : (ObjectUtils.isEmpty(eventCode) ? userDao.getAllUsersBySearch(pageable, search) : userDao.getAllUsersWithSearchAndEventCode(pageable, search, eventCode));
        List<Event> eventList = eventDao.findAllEvents();
        Map<String, Event> eventMap = eventList.stream()
                .collect(Collectors.toMap(Event::getEventCode, event -> event));
        List<UserResponse> userResponseList = users.stream()
                .map(user -> {
                    UserResponse userResponse = mapper.convertValue(user, UserResponse.class);
                    Event event = eventMap.get(user.getEventCode());
                    if (ObjectUtils.isNotEmpty(event)) {
                        userResponse.setZone(event.getZone());
                        userResponse.setGeneralOffice(event.getGeneralOffice());
                        userResponse.setEventName(event.getEventName());
                        userResponse.setEventStartDate(event.getEventStartDate());
                    }
                    return userResponse;
                })
                .collect(Collectors.toList());

        return ResponseDto.<UserResponse>builder()
                .status(RESPONSE_SUCCESS)
                .data(userResponseList)
                .count(userResponseList.stream().count())
                .total(users.getTotalElements())
                .build();
    }

    public void downloadCSVFile(@PageableDefault Pageable pageable, String eventCode, HttpServletResponse response) {
        if(StringUtils.isEmpty(eventCode)){
            throw new CoffeeException(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "Event Code"));
        }
        try {
            byte[] fileData = downloadCSVFile(pageable, eventCode);
            log.info("Successfully retrieved CSV data for download user.");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=users.csv");
            response.getOutputStream().write(fileData);
            response.flushBuffer();
            log.info("User CSV file written to output stream successfully.");
        }catch (Exception ex) {
            log.error("An unexpected error occurred while generating user csv: {}", ex.getMessage());
            throw new CoffeeException(ErrorConstants.SYSTEM_ERROR_CODE, ex.getMessage());
        }
    }

    public void updateIsActiveById(UUID userId, Boolean isActive){
        userDao.updateIsActiveById(userId, isActive);
    }

    private byte[] downloadCSVFile(@PageableDefault Pageable pageable,String eventCode) {
        log.info("Starting to download CSV file with eventCode: '{}'", eventCode);
        EventResponse eventResponse = eventService.getEventByEventCode(eventCode);
        Page<User> userList = userDao.getUsersByEventCode(pageable, eventCode);
        return SFTPFileUtils.createSFTPUserWorkSheet(userList, eventResponse);
    }
}
