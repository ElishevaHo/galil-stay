package com.galilstay.server.web;

import com.galilstay.server.dto.GuestDto;
import com.galilstay.server.dto.ReservationDto;
import com.galilstay.server.service.GuestService;
import com.galilstay.server.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService service;
    private final ReservationService reservationService;

    @GetMapping
    public List<GuestDto> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GuestDto one(@PathVariable Long id) {
        return service.findOne(id);
    }

    @GetMapping("/{id}/reservations")
    public List<ReservationDto> history(@PathVariable Long id) {
        return reservationService.findByGuest(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestDto create(@Valid @RequestBody GuestDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public GuestDto update(@PathVariable Long id, @Valid @RequestBody GuestDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
