package com.galilstay.server.dto;

import com.galilstay.server.model.ReservationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** הזמנה כפי שהיא נשלחת ללקוח */
@Data
public class ReservationDto {
    private Long id;
    private Long guestId;
    private String guestName;
    private Long unitId;
    private String unitName;
    private LocalDate arrival;
    private LocalDate departure;
    private long nights;
    private int adults;
    private int children;
    private List<Long> extraIds;
    private List<String> extraTitles;
    private ReservationStatus status;
    private double amount;
    private double refund;
    private String requests;
    private LocalDateTime createdAt;
    private boolean reviewed;
}
