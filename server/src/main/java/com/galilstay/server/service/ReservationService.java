package com.galilstay.server.service;

import com.galilstay.server.dto.BookingRequest;
import com.galilstay.server.dto.PriceQuote;
import com.galilstay.server.dto.ReservationDto;
import com.galilstay.server.exception.NotFoundException;
import com.galilstay.server.exception.RuleViolationException;
import com.galilstay.server.model.*;
import com.galilstay.server.repository.ReservationRepository;
import com.galilstay.server.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservations;
    private final ReviewRepository reviews;
    private final UnitService unitService;
    private final GuestService guestService;
    private final ExtraService extraService;
    private final PricingService pricing;
    private final ModelMapper mapper;

    public List<ReservationDto> findAll(ReservationStatus status) {
        return reservations.findAllByOrderByArrivalDesc().stream()
                .filter(r -> status == null || r.getStatus() == status)
                .map(this::toDto)
                .toList();
    }

    public ReservationDto findOne(Long id) {
        return toDto(load(id));
    }

    public List<ReservationDto> findByGuest(Long guestId) {
        return reservations.findByGuestIdOrderByArrivalDesc(guestId).stream().map(this::toDto).toList();
    }

    /** הזמנות פעילות בטווח תאריכים - עבור תצוגת לוח הזמנים */
    public List<ReservationDto> findBetween(LocalDate from, LocalDate to) {
        return reservations.findActiveBetween(from, to).stream()
                .sorted(Comparator.comparing(Reservation::getArrival))
                .map(this::toDto)
                .toList();
    }

    /** הצעת מחיר בלי לשמור הזמנה */
    public PriceQuote quote(BookingRequest request) {
        Unit unit = unitService.load(request.getUnitId());
        validate(request, unit);
        return pricing.quote(unit, request.getArrival(), request.getDeparture(),
                extraService.loadAll(request.getExtraIds()));
    }

    @Transactional
    public ReservationDto create(BookingRequest request) {
        Unit unit = unitService.load(request.getUnitId());
        Guest guest = guestService.load(request.getGuestId());
        validate(request, unit);

        if (reservations.isUnitBooked(unit.getId(), request.getArrival(), request.getDeparture()))
            throw new RuleViolationException("היחידה " + unit.getName() + " תפוסה בתאריכים האלה");

        List<Extra> extras = extraService.loadAll(request.getExtraIds());
        PriceQuote quote = pricing.quote(unit, request.getArrival(), request.getDeparture(), extras);

        Reservation reservation = new Reservation();
        reservation.setGuest(guest);
        reservation.setUnit(unit);
        reservation.setArrival(request.getArrival());
        reservation.setDeparture(request.getDeparture());
        reservation.setAdults(request.getAdults());
        reservation.setChildren(request.getChildren());
        reservation.setExtras(new HashSet<>(extras));
        reservation.setRequests(request.getRequests());
        reservation.setAmount(quote.total());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        return toDto(reservations.save(reservation));
    }

    public ReservationDto confirm(Long id) {
        Reservation reservation = load(id);
        if (reservation.getStatus() != ReservationStatus.PENDING)
            throw new RuleViolationException("אפשר לאשר רק הזמנה שממתינה לאישור");
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return toDto(reservations.save(reservation));
    }

    public ReservationDto cancel(Long id) {
        Reservation reservation = load(id);
        ReservationStatus status = reservation.getStatus();
        if (status == ReservationStatus.CANCELLED || status == ReservationStatus.COMPLETED)
            throw new RuleViolationException("ההזמנה כבר " + (status == ReservationStatus.CANCELLED ? "בוטלה" : "הסתיימה"));

        LocalDate today = LocalDate.now();
        if (!today.isBefore(reservation.getArrival()))
            throw new RuleViolationException("לא ניתן לבטל הזמנה שהשהייה בה כבר התחילה");

        reservation.setRefund(pricing.refundFor(reservation, today));
        reservation.setStatus(ReservationStatus.CANCELLED);
        return toDto(reservations.save(reservation));
    }

    public ReservationDto complete(Long id) {
        Reservation reservation = load(id);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED)
            throw new RuleViolationException("אפשר לסגור רק הזמנה מאושרת");
        if (reservation.getDeparture().isAfter(LocalDate.now()))
            throw new RuleViolationException("האורחים עדיין לא עזבו - תאריך העזיבה הוא " + reservation.getDeparture());
        reservation.setStatus(ReservationStatus.COMPLETED);
        return toDto(reservations.save(reservation));
    }

    /** כמה יוחזר אם נבטל עכשיו - לתצוגה לפני אישור הביטול */
    public double previewRefund(Long id) {
        return pricing.refundFor(load(id), LocalDate.now());
    }

    Reservation load(Long id) {
        return reservations.findById(id).orElseThrow(() -> new NotFoundException("הזמנה", id));
    }

    private void validate(BookingRequest request, Unit unit) {
        if (!request.getDeparture().isAfter(request.getArrival()))
            throw new RuleViolationException("תאריך העזיבה חייב להיות אחרי תאריך ההגעה");
        if (request.getArrival().isBefore(LocalDate.now()))
            throw new RuleViolationException("לא ניתן להזמין לתאריך שעבר");
        if (!unit.isListed())
            throw new RuleViolationException("היחידה " + unit.getName() + " לא פתוחה כרגע להזמנות");

        long nights = ChronoUnit.DAYS.between(request.getArrival(), request.getDeparture());
        if (nights < unit.getMinNights())
            throw new RuleViolationException("ביחידה " + unit.getName() + " יש מינימום של " + unit.getMinNights() + " לילות");

        int people = request.getAdults() + request.getChildren();
        if (people > unit.getCapacity())
            throw new RuleViolationException("היחידה מתאימה לעד " + unit.getCapacity() + " אורחים");
    }

    ReservationDto toDto(Reservation r) {
        ReservationDto dto = mapper.map(r, ReservationDto.class);
        dto.setGuestId(r.getGuest().getId());
        dto.setUnitId(r.getUnit().getId());
        dto.setUnitName(r.getUnit().getName());
        dto.setGuestName(r.getGuest().getFirstName() + " " + r.getGuest().getLastName());
        dto.setNights(ChronoUnit.DAYS.between(r.getArrival(), r.getDeparture()));
        dto.setExtraIds(r.getExtras().stream().map(Extra::getId).toList());
        dto.setExtraTitles(r.getExtras().stream().map(Extra::getTitle).toList());
        dto.setReviewed(reviews.existsByReservationId(r.getId()));
        return dto;
    }
}
