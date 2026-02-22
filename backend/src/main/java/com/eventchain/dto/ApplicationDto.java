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
public class ApplicationDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private String status;
    private Integer applicationOrder;
    private Integer lotteryRound;
    private LocalDateTime createdAt;
    private String userEmail;
    private String userDisplayName;
}
