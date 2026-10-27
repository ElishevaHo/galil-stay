package com.galilstay.server.service;

import com.galilstay.server.dto.PriceQuote;
import com.galilstay.server.model.Extra;
import com.galilstay.server.model.Reservation;
import com.galilstay.server.model.Unit;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

/**
 * חישובי מחיר:
 * - לילות חמישי ושישי מחויבים בתעריף סוף שבוע
 * - הנחת שהייה ארוכה: 4+ לילות 6%, 7+ לילות 12%
 * - תוספות לפי לילה או חד-פעמיות + דמי ניקיון
 */
@Service
public class PricingService {

    public PriceQuote quote(Unit unit, LocalDate arrival, LocalDate departure, Collection<Extra> extras) {
        long weekendNights = 0;
        long weekdayNights = 0;
        for (LocalDate night = arrival; night.isBefore(departure); night = night.plusDays(1)) {
            if (isWeekendNight(night)) weekendNights++;
            else weekdayNights++;
        }
        long nights = weekdayNights + weekendNights;

        double lodging = weekdayNights * unit.getWeekdayRate() + weekendNights * unit.getWeekendRate();
        int discountPercent = longStayDiscount(nights);
        double discount = round(lodging * discountPercent / 100.0);

        double extrasTotal = extras.stream()
                .mapToDouble(e -> e.isChargedPerNight() ? e.getPrice() * nights : e.getPrice())
                .sum();

        double total = round(lodging - discount + extrasTotal + unit.getCleaningFee());
        return new PriceQuote(nights, weekdayNights, weekendNights, round(lodging), discountPercent,
                discount, round(extrasTotal), unit.getCleaningFee(), total);
    }

    /** מדיניות החזר: 14+ ימים לפני ההגעה - מלא, 5-13 ימים - חצי, פחות מזה - ללא החזר */
    public double refundFor(Reservation reservation, LocalDate today) {
        long daysBefore = ChronoUnit.DAYS.between(today, reservation.getArrival());
        if (daysBefore >= 14) return reservation.getAmount();
        if (daysBefore >= 5) return round(reservation.getAmount() / 2);
        return 0;
    }

    private boolean isWeekendNight(LocalDate night) {
        DayOfWeek day = night.getDayOfWeek();
        return day == DayOfWeek.THURSDAY || day == DayOfWeek.FRIDAY;
    }

    private int longStayDiscount(long nights) {
        if (nights >= 7) return 12;
        if (nights >= 4) return 6;
        return 0;
    }

    private double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
