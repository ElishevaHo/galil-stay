package com.galilstay.server.model;

/** מחזור החיים של הזמנה: ממתינה -> מאושרת -> הסתיימה, או בוטלה */
public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}
