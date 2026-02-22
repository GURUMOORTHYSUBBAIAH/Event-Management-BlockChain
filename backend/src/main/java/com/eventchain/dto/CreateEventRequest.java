package com.eventchain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String category;

    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;

    private String location;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Max seats is required")
    @Min(value = 1, message = "Max seats must be at least 1")
    private Integer maxSeats;

    @NotNull(message = "Lottery deadline is required")
    private LocalDateTime lotteryDeadline;
}
