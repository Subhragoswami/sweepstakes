package com.coffee.sweepstakes.service;

import com.coffee.sweepstakes.dao.EventDao;
import com.coffee.sweepstakes.entity.Event;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.model.response.EventResponse;
import com.coffee.sweepstakes.util.AppConstants;
import com.coffee.sweepstakes.util.DateUtils;
import com.coffee.sweepstakes.util.ErrorConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.coffee.sweepstakes.model.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventDao eventDao;
    private final ObjectMapper mapper;

    public ResponseDto<EventResponse> getAllEvents(String eventCode) {
        log.info("Fetching events with eventCode: {}", eventCode);
        List<Event> events;
        if (ObjectUtils.isNotEmpty(eventCode)) {
            log.info("Looking for event with eventCode: {}", eventCode);
            events = List.of(eventDao.findByEventCode(eventCode).orElseThrow(() -> new CoffeeException(ErrorConstants.NOT_FOUND_ERROR_CODE, ErrorConstants.NOT_FOUND_ERROR_MESSAGE)));
        } else {
            log.info("Fetching all events as no specific eventCode provided.");
            events = eventDao.findAllEvents();
        }

        List<EventResponse> eventResponseList = events.stream().map((event) -> {
            EventResponse eventResponse = mapper.convertValue(event, EventResponse.class);
            boolean isRegisterActive = isRegistrationPeriodActive(event.getEventStartDate(), event.getEventEndDate());
            eventResponse.setRegistrationActive(isRegisterActive);
            log.info("Registration status for event code {}: {}", event.getEventCode(), isRegisterActive);
            return eventResponse;
        }).collect(Collectors.toList());
        log.info("Total event responses created: {}", eventResponseList.size());
        return ResponseDto.<EventResponse>builder()
                .status(AppConstants.RESPONSE_SUCCESS)
                .data(eventResponseList)
                .count(eventResponseList.stream().count())
                .total((long) events.size())
                .build();
    }
    public EventResponse getEventByEventCode(String eventCode) {
        Optional<Event> event = eventDao.findByEventCode(eventCode);
        return mapper.convertValue(event, EventResponse.class);
    }

    private boolean isRegistrationPeriodActive(Date startDate, Date endDate) {
        log.info("Checking registration period: startDate = {}, endDate = {}", startDate, endDate);
        if(ObjectUtils.isEmpty(startDate) && ObjectUtils.isEmpty(endDate)){
            return false;
        }
        Date currentDate = DateUtils.getAppDate();
        return !currentDate.before(startDate) && !currentDate.after(endDate);
    }

}
