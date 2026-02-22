package com.eventchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAnalyticsDto {
    private Long eventId;
    private String eventTitle;
    private String status;
    private Long totalApplicants;
    private Long selectedCount;
    private Long waitlistedCount;
    private Long paidCount;
    private Long nftsMinted;
    private Long checkedInCount;
    private Long certificatesIssued;
    private BigDecimal revenue;
    private Double paymentPercentage;
    private Double noShowRate;
}
