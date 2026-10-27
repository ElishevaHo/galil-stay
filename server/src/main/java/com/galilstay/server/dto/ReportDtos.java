package com.galilstay.server.dto;

import java.util.List;

/** אובייקטים של דוחות ולוח הבקרה */
public final class ReportDtos {

    private ReportDtos() {
    }

    public record Overview(
            long listedUnits,
            long arrivalsToday,
            long departuresToday,
            long staysInHouse,
            long pendingApprovals,
            double revenueThisMonth,
            int occupancyThisMonth,
            Double averageStars,
            List<ReservationDto> nextArrivals
    ) {
    }

    public record MonthRow(int month, double revenue, long nightsSold, int occupancy) {
    }

    public record UnitRow(Long unitId, String unitName, long reservations, long nightsSold,
                          double revenue, Double averageStars) {
    }
}
