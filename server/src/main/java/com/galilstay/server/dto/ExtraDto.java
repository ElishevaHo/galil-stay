package com.galilstay.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ExtraDto {
    private Long id;

    @NotBlank(message = "יש להזין שם לתוספת")
    private String title;

    @PositiveOrZero(message = "מחיר לא יכול להיות שלילי")
    private double price;

    private boolean chargedPerNight;
}
