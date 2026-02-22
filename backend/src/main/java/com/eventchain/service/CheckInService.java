package com.eventchain.service;

import com.eventchain.dto.TicketDto;
import com.eventchain.entity.Ticket;
import com.eventchain.exception.BadRequestException;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckInService {
    private final TicketRepository ticketRepository;
    private final UserService userService;

    @Autowired(required = false)
    private com.eventchain.blockchain.NftContractService nftContractService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public TicketDto checkIn(Long eventId, Long tokenId, org.springframework.security.core.Authentication auth) {
        Ticket ticket = ticketRepository.findByEventIdAndTokenId(eventId, tokenId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        if (ticket.getCheckedIn()) {
            throw new BadRequestException("Already checked in");
        }

        if (nftContractService != null) {
            try {
                String walletAddress = ticket.getUser().getWalletAddress();
                if (walletAddress != null && nftContractService.verifyOwnership(walletAddress, tokenId)) {
                    nftContractService.markAttendance(tokenId);
                }
            } catch (Exception e) {
                // Continue - we may not have blockchain
            }
        }

        ticket.setCheckedIn(true);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/event/" + eventId + "/checkin",
                    new CheckInEvent(eventId, tokenId, ticket.getUser().getDisplayName()));
        }

        return toDto(ticket);
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

    public record CheckInEvent(Long eventId, Long tokenId, String attendeeName) {}
}
