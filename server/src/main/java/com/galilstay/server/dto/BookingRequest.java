package com.galilstay.server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** בקשה ליצירת הזמנה חדשה או לחישוב הצעת מחיר */
@Data
public class BookingRequest {

    @NotNull(message = "יש לבחור אורח")
    private Long guestId;

    @NotNull(message = "יש לבחור יחידה")
    private Long unitId;

    @NotNull(message = "יש לבחור תאריך הגעה")
    private LocalDate arrival;

    @NotNull(message = "יש לבחור תאריך עזיבה")
    private LocalDate departure;

    @Min(value = 1, message = "לפחות מבוגר אחד")
    private int adults = 1;

    @Min(value = 0, message = "מספר ילדים לא תקין")
    private int children;

    private List<Long> extraIds = new ArrayList<>();

    private String requests;
}
