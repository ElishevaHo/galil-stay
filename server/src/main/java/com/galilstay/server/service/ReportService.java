package com.galilstay.server.service;

import com.galilstay.server.dto.ReportDtos.MonthRow;
import com.galilstay.server.dto.ReportDtos.Overview;
import com.galilstay.server.dto.ReportDtos.UnitRow;
import com.galilstay.server.dto.ReservationDto;
import com.galilstay.server.model.Reservation;
import com.galilstay.server.model.ReservationStatus;
import com.galilstay.server.model.Unit;
import com.galilstay.server.repository.ReservationRepository;
import com.galilstay.server.repository.ReviewRepository;
import com.galilstay.server.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReservationRepository reservations;
    private final UnitRepository units;
    private final ReviewRepository reviews;
    private final ReservationService reservationService;

    public Overview overview() {
        LocalDate today = LocalDate.now();
        List<Reservation> active = reservations.findByStatusNot(ReservationStatus.CANCELLED);

        long arrivals = active.stream().filter(r -> r.getArrival().equals(today)).count();
        long departures = active.stream().filter(r -> r.getDeparture().equals(today)).count();
        long inHouse = active.stream()
                .filter(r -> !r.getArrival().isAfter(today) && r.getDeparture().isAfter(today))
                .count();

        List<ReservationDto> nextArrivals = active.stream()
                .filter(r -> !r.getArrival().isBefore(today) && r.getArrival().isBefore(today.plusDays(8)))
                .sorted(Comparator.comparing(Reservation::getArrival))
                .limit(6)
                .map(reservationService::toDto)
                .toList();

        MonthRow thisMonth = month(YearMonth.from(today), active);
        Double avg = reviews.averageOverall();

        return new Overview(
                units.countByListedTrue(),
                arrivals,
                departures,
                inHouse,
                reservations.countByStatus(ReservationStatus.PENDING),
                thisMonth.revenue(),
                thisMonth.occupancy(),
                avg == null ? null : Math.round(avg * 10) / 10.0,
                nextArrivals
        );
    }

    /** הכנסות, לילות ותפוסה לכל חודש בשנה */
    public List<MonthRow> yearlyBreakdown(int year) {
        List<Reservation> active = reservations.findByStatusNot(ReservationStatus.CANCELLED);
        List<MonthRow> rows = new ArrayList<>();
        for (int m = 1; m <= 12; m++) rows.add(month(YearMonth.of(year, m), active));
        return rows;
    }

    /** ביצועים לפי יחידה */
    public List<UnitRow> unitPerformance() {
        List<UnitRow> rows = new ArrayList<>();
        for (Unit unit : units.findAll()) {
            List<Reservation> list = reservations.findByUnitId(unit.getId()).stream()
                    .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                    .toList();
            long nights = list.stream().mapToLong(r -> r.getDeparture().toEpochDay() - r.getArrival().toEpochDay()).sum();
            double revenue = list.stream().mapToDouble(Reservation::getAmount).sum();
            Double avg = reviews.averageForUnit(unit.getId());
            rows.add(new UnitRow(unit.getId(), unit.getName(), list.size(), nights, revenue,
                    avg == null ? null : Math.round(avg * 10) / 10.0));
        }
        rows.sort(Comparator.comparingDouble(UnitRow::revenue).reversed());
        return rows;
    }

    /**
     * הכנסה נזקפת לחודש לפי מספר הלילות שנפלו בו (הזמנה שחוצה חודשים מתחלקת באופן יחסי).
     * תפוסה = לילות שנמכרו / (יחידות פעילות * ימים בחודש)
     */
    private MonthRow month(YearMonth ym, List<Reservation> active) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);
        long nightsSold = 0;
        double revenue = 0;

        for (Reservation r : active) {
            LocalDate from = r.getArrival().isAfter(start) ? r.getArrival() : start;
            LocalDate to = r.getDeparture().isBefore(end) ? r.getDeparture() : end;
            long overlap = to.toEpochDay() - from.toEpochDay();
            if (overlap <= 0) continue;
            long total = r.getDeparture().toEpochDay() - r.getArrival().toEpochDay();
            nightsSold += overlap;
            revenue += r.getAmount() * overlap / total;
        }

        long capacity = units.countByListedTrue() * ym.lengthOfMonth();
        int occupancy = capacity == 0 ? 0 : (int) Math.round(100.0 * nightsSold / capacity);
        return new MonthRow(ym.getMonthValue(), Math.round(revenue), nightsSold, Math.min(occupancy, 100));
    }
}
