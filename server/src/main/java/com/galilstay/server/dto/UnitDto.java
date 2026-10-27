package com.galilstay.server.dto;

import com.galilstay.server.model.UnitType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UnitDto {
    private Long id;

    @NotBlank(message = "יש להזין שם ליחידה")
    private String name;

    @NotNull(message = "יש לבחור סוג יחידה")
    private UnitType kind;

    private String region;

    @Positive(message = "מחיר אמצע שבוע חייב להיות חיובי")
    private double weekdayRate;

    @Positive(message = "מחיר סוף שבוע חייב להיות חיובי")
    private double weekendRate;

    @Min(value = 0, message = "דמי ניקיון לא יכולים להיות שליליים")
    private double cleaningFee;

    @Min(value = 1, message = "קיבולת מינימלית היא אורח אחד")
    private int capacity;

    @Min(value = 1, message = "לפחות חדר שינה אחד")
    private int bedrooms;

    @Min(value = 1, message = "מינימום לילה אחד")
    private int minNights;

    private boolean privatePool;
    private boolean jacuzzi;
    private boolean petsAllowed;
    private String description;
    private boolean listed;

    // שדות מחושבים - לקריאה בלבד
    private Double averageStars;
    private int reviewCount;
}
