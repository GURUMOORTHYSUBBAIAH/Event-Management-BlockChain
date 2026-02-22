package com.eventchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    private Long id;
    private Long applicationId;
    private Long eventId;
    private Long userId;
    private Long tokenId;
    private String transactionHash;
    private Boolean checkedIn;
    private LocalDateTime checkedInAt;
    private LocalDateTime createdAt;
}
