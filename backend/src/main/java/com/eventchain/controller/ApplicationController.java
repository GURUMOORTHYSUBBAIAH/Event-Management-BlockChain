package com.eventchain.controller;

import com.eventchain.dto.ApplicationDto;
import com.eventchain.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final com.eventchain.service.UserService userService;

    @PostMapping("/events/{eventId}/apply")
    public ResponseEntity<ApplicationDto> apply(@PathVariable Long eventId, Authentication auth) {
        return ResponseEntity.ok(applicationService.apply(eventId, auth));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<ApplicationDto>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(applicationService.getApplicationsByEvent(eventId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ApplicationDto>> getMyApplications(Authentication auth) {
        var user = userService.getCurrentUser(auth);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(applicationService.getMyApplications(user.getId()));
    }
}
