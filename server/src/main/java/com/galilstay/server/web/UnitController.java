package com.galilstay.server.web;

import com.galilstay.server.dto.AvailableUnit;
import com.galilstay.server.dto.UnitDto;
import com.galilstay.server.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService service;

    @GetMapping
    public List<UnitDto> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UnitDto one(@PathVariable Long id) {
        return service.findOne(id);
    }

    @GetMapping("/available")
    public List<AvailableUnit> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate arrival,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departure,
            @RequestParam(defaultValue = "2") int guests) {
        return service.searchAvailable(arrival, departure, guests);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnitDto create(@Valid @RequestBody UnitDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public UnitDto update(@PathVariable Long id, @Valid @RequestBody UnitDto dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}/listing")
    public UnitDto toggleListing(@PathVariable Long id) {
        return service.toggleListed(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
