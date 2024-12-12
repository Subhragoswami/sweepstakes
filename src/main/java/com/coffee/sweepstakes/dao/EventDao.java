package com.coffee.sweepstakes.dao;

import com.coffee.sweepstakes.entity.Event;
import com.coffee.sweepstakes.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class EventDao {
    private final EventRepository eventRepository;

    public List<Event> findAllEvents() {
        return eventRepository.findAllByOrderByEventStartDateAsc();
    }

    public Optional<Event> findByEventCode(String eventCode) {
        return eventRepository.findByEventCode(eventCode);
    }
}
