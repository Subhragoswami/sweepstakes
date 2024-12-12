package com.coffee.sweepstakes.repository;

import com.coffee.sweepstakes.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByEventCode(String eventCode);

    List<Event> findAllByOrderByEventStartDateAsc();
}
