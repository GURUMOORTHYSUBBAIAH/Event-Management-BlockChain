package com.eventchain.controller;

import com.eventchain.dto.TicketDto;
import com.eventchain.entity.Ticket;
import com.eventchain.repository.TicketRepository;
import com.eventchain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketRepository ticketRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<List<TicketDto>> getMyTickets(Authentication auth) {
        var user = userService.getCurrentUser(auth);
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(ticketRepository.findByUserId(user.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketDto>> getByEvent(@PathVariable Long eventId, Authentication auth) {
        return ResponseEntity.ok(ticketRepository.findByEventId(eventId).stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
    }

    private TicketDto toDto(Ticket t) {
        return TicketDto.builder()
                .id(t.getId())
                .applicationId(t.getApplication().getId())
                .eventId(t.getEvent().getId())
                .userId(t.getUser().getId())
                .tokenId(t.getTokenId())
                .transactionHash(t.getTransactionHash())
                .checkedIn(t.getCheckedIn())
                .checkedInAt(t.getCheckedInAt())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
