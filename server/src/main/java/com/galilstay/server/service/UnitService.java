package com.galilstay.server.service;

import com.galilstay.server.dto.AvailableUnit;
import com.galilstay.server.dto.UnitDto;
import com.galilstay.server.exception.NotFoundException;
import com.galilstay.server.exception.RuleViolationException;
import com.galilstay.server.model.Unit;
import com.galilstay.server.repository.ReservationRepository;
import com.galilstay.server.repository.ReviewRepository;
import com.galilstay.server.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository units;
    private final ReservationRepository reservations;
    private final ReviewRepository reviews;
    private final PricingService pricing;
    private final ModelMapper mapper;

    public List<UnitDto> findAll() {
        return units.findAll().stream().map(this::toDto).toList();
    }

    public UnitDto findOne(Long id) {
        return toDto(load(id));
    }

    public UnitDto create(UnitDto dto) {
        if (units.existsByNameIgnoreCase(dto.getName()))
            throw new RuleViolationException("כבר קיימת יחידה בשם " + dto.getName());
        checkRates(dto);
        Unit unit = mapper.map(dto, Unit.class);
        unit.setId(null);
        return toDto(units.save(unit));
    }

    public UnitDto update(Long id, UnitDto dto) {
        Unit unit = load(id);
        if (!unit.getName().equalsIgnoreCase(dto.getName()) && units.existsByNameIgnoreCase(dto.getName()))
            throw new RuleViolationException("כבר קיימת יחידה בשם " + dto.getName());
        checkRates(dto);
        mapper.map(dto, unit);
        unit.setId(id);
        return toDto(units.save(unit));
    }

    /** יחידה עם היסטוריית הזמנות לא נמחקת - רק מוסתרת */
    public void delete(Long id) {
        Unit unit = load(id);
        if (reservations.existsByUnitId(id))
            throw new RuleViolationException("ליחידה יש הזמנות. אפשר להסתיר אותה במקום למחוק");
        units.delete(unit);
    }

    public UnitDto toggleListed(Long id) {
        Unit unit = load(id);
        unit.setListed(!unit.isListed());
        return toDto(units.save(unit));
    }

    /** חיפוש יחידות פנויות לפי תאריכים ומספר אורחים */
    public List<AvailableUnit> searchAvailable(LocalDate arrival, LocalDate departure, int guests) {
        if (!departure.isAfter(arrival))
            throw new RuleViolationException("תאריך העזיבה חייב להיות אחרי תאריך ההגעה");
        long nights = departure.toEpochDay() - arrival.toEpochDay();

        return units.findByListedTrueAndCapacityGreaterThanEqualOrderByWeekdayRate(guests).stream()
                .filter(u -> nights >= u.getMinNights())
                .filter(u -> !reservations.isUnitBooked(u.getId(), arrival, departure))
                .map(u -> new AvailableUnit(toDto(u), pricing.quote(u, arrival, departure, List.of())))
                .toList();
    }

    Unit load(Long id) {
        return units.findById(id).orElseThrow(() -> new NotFoundException("יחידה", id));
    }

    private void checkRates(UnitDto dto) {
        if (dto.getWeekendRate() < dto.getWeekdayRate())
            throw new RuleViolationException("מחיר סוף שבוע לא יכול להיות נמוך ממחיר אמצע שבוע");
    }

    private UnitDto toDto(Unit unit) {
        UnitDto dto = mapper.map(unit, UnitDto.class);
        Double avg = reviews.averageForUnit(unit.getId());
        dto.setAverageStars(avg == null ? null : Math.round(avg * 10) / 10.0);
        dto.setReviewCount(reviews.findByReservationUnitId(unit.getId()).size());
        return dto;
    }
}
