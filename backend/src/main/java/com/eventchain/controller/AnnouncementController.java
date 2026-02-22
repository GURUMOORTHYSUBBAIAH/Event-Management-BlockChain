package com.eventchain.controller;

import com.eventchain.dto.AnnouncementDto;
import com.eventchain.dto.CreateAnnouncementRequest;
import com.eventchain.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','EVENT_HEAD','TEAM_MEMBER')")
    public ResponseEntity<AnnouncementDto> create(@Valid @RequestBody CreateAnnouncementRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(announcementService.create(request, auth));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<AnnouncementDto>> getByEvent(@PathVariable Long eventId, Authentication auth) {
        return ResponseEntity.ok(announcementService.getByEvent(eventId, auth));
    }

    @GetMapping("/public")
    public ResponseEntity<List<AnnouncementDto>> getPublic() {
        return ResponseEntity.ok(announcementService.getPublic());
    }
}
