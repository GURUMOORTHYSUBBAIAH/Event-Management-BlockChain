package com.eventchain.controller;

import com.eventchain.dto.*;
import com.eventchain.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventDto>> getEvents(
            @RequestParam(required = false, defaultValue = "OPEN") String status,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.getEvents(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','EVENT_HEAD')")
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody CreateEventRequest request, Authentication auth) {
        return ResponseEntity.ok(eventService.createEvent(request, auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','EVENT_HEAD')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @Valid @RequestBody CreateEventRequest request,
                                                Authentication auth) {
        return ResponseEntity.ok(eventService.updateEvent(id, request, auth));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','EVENT_HEAD')")
    public ResponseEntity<EventDto> publishEvent(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(eventService.publishEvent(id, auth));
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','EVENT_HEAD','TEAM_MEMBER')")
    public ResponseEntity<EventAnalyticsDto> getAnalytics(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(eventService.getAnalytics(id, auth));
    }
}
