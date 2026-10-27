package com.galilstay.server.web;

import com.galilstay.server.dto.ExtraDto;
import com.galilstay.server.service.ExtraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extras")
@RequiredArgsConstructor
public class ExtraController {

    private final ExtraService service;

    @GetMapping
    public List<ExtraDto> all() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExtraDto create(@Valid @RequestBody ExtraDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ExtraDto update(@PathVariable Long id, @Valid @RequestBody ExtraDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
