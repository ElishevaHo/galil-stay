package com.galilstay.server.service;

import com.galilstay.server.dto.GuestDto;
import com.galilstay.server.exception.NotFoundException;
import com.galilstay.server.exception.RuleViolationException;
import com.galilstay.server.model.Guest;
import com.galilstay.server.model.Reservation;
import com.galilstay.server.model.ReservationStatus;
import com.galilstay.server.repository.GuestRepository;
import com.galilstay.server.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guests;
    private final ReservationRepository reservations;
    private final ModelMapper mapper;

    public List<GuestDto> findAll() {
        return guests.findAll().stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(GuestDto::getLastName))
                .toList();
    }

    public GuestDto findOne(Long id) {
        return toDto(load(id));
    }

    public GuestDto create(GuestDto dto) {
        if (guests.existsByEmailIgnoreCase(dto.getEmail()))
            throw new RuleViolationException("אורח עם האימייל הזה כבר קיים במערכת");
        Guest guest = mapper.map(dto, Guest.class);
        guest.setId(null);
        guest.setEmail(dto.getEmail().trim().toLowerCase());
        guest.setJoinedOn(LocalDate.now());
        return toDto(guests.save(guest));
    }

    public GuestDto update(Long id, GuestDto dto) {
        Guest guest = load(id);
        if (!guest.getEmail().equalsIgnoreCase(dto.getEmail()) && guests.existsByEmailIgnoreCase(dto.getEmail()))
            throw new RuleViolationException("אורח עם האימייל הזה כבר קיים במערכת");
        guest.setFirstName(dto.getFirstName());
        guest.setLastName(dto.getLastName());
        guest.setEmail(dto.getEmail().trim().toLowerCase());
        guest.setPhone(dto.getPhone());
        guest.setCity(dto.getCity());
        return toDto(guests.save(guest));
    }

    public void delete(Long id) {
        Guest guest = load(id);
        if (reservations.existsByGuestId(id))
            throw new RuleViolationException("לא ניתן למחוק אורח שיש לו הזמנות");
        guests.delete(guest);
    }

    Guest load(Long id) {
        return guests.findById(id).orElseThrow(() -> new NotFoundException("אורח", id));
    }

    private GuestDto toDto(Guest guest) {
        GuestDto dto = mapper.map(guest, GuestDto.class);
        List<Reservation> history = reservations.findByGuestIdOrderByArrivalDesc(guest.getId()).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .toList();
        dto.setStays(history.size());
        dto.setTotalSpent(history.stream().mapToDouble(Reservation::getAmount).sum());
        return dto;
    }
}
