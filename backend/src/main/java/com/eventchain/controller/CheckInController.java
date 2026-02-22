package com.eventchain.controller;

import com.eventchain.dto.TicketDto;
import com.eventchain.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {
    private final CheckInService checkInService;

    @PostMapping("/events/{eventId}/tickets/{tokenId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','EVENT_HEAD','TEAM_MEMBER')")
    public ResponseEntity<TicketDto> checkIn(@PathVariable Long eventId, @PathVariable Long tokenId,
                                             Authentication auth) {
        return ResponseEntity.ok(checkInService.checkIn(eventId, tokenId, auth));
    }
}
