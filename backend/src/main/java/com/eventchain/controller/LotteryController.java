package com.eventchain.controller;

import com.eventchain.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class LotteryController {
    private final LotteryService lotteryService;

    @PostMapping("/{eventId}/lottery/trigger")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN','EVENT_HEAD')")
    public ResponseEntity<Void> triggerLottery(@PathVariable Long eventId, Authentication auth) {
        lotteryService.triggerLottery(eventId, auth);
        return ResponseEntity.ok().build();
    }
}
