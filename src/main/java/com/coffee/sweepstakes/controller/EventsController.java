package com.coffee.sweepstakes.controller;

import com.coffee.sweepstakes.model.response.EventResponse;
import com.coffee.sweepstakes.model.response.ResponseDto;
import com.coffee.sweepstakes.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventsController {
    private final EventService eventService;
    @GetMapping
    public ResponseDto<EventResponse> getEvents(@RequestParam(required = false) String eventCode) {
        log.info("received request for getting all event");
        return eventService.getAllEvents(eventCode);
    }
}
