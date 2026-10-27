package com.galilstay.server.web;

import com.galilstay.server.dto.BookingRequest;
import com.galilstay.server.dto.PriceQuote;
import com.galilstay.server.dto.ReservationDto;
import com.galilstay.server.model.ReservationStatus;
import com.galilstay.server.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @GetMapping
    public List<ReservationDto> all(@RequestParam(required = false) ReservationStatus status) {
        return service.findAll(status);
    }

    @GetMapping("/{id}")
    public ReservationDto one(@PathVariable Long id) {
        return service.findOne(id);
    }

    @GetMapping("/timeline")
    public List<ReservationDto> timeline(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findBetween(from, to);
    }

    @PostMapping("/quote")
    public PriceQuote quote(@Valid @RequestBody BookingRequest request) {
        return service.quote(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDto create(@Valid @RequestBody BookingRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}/refund")
    public Map<String, Double> refundPreview(@PathVariable Long id) {
        return Map.of("refund", service.previewRefund(id));
    }

    @PatchMapping("/{id}/confirm")
    public ReservationDto confirm(@PathVariable Long id) {
        return service.confirm(id);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationDto cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PatchMapping("/{id}/complete")
    public ReservationDto complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
