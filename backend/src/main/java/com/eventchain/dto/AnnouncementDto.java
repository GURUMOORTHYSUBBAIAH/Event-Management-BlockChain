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
public class AnnouncementDto {
    private Long id;
    private Long eventId;
    private String title;
    private String content;
    private String type;
    private Long createdBy;
    private LocalDateTime createdAt;
}
