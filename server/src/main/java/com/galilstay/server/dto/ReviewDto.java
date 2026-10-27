package com.galilstay.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReviewDto {
    private Long id;

    @NotNull(message = "יש לבחור הזמנה")
    private Long reservationId;

    @Min(value = 1, message = "דירוג בין 1 ל-5")
    @Max(value = 5, message = "דירוג בין 1 ל-5")
    private int stars;

    private String text;
    private LocalDate writtenOn;

    // לתצוגה
    private String unitName;
    private String guestName;
}
