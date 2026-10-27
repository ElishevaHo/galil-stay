package com.galilstay.server.dto;

/** פירוט מחיר של שהייה */
public record PriceQuote(
        long nights,
        long weekdayNights,
        long weekendNights,
        double lodging,
        int discountPercent,
        double discount,
        double extras,
        double cleaningFee,
        double total
) {
}
