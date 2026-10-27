package com.galilstay.server.service;

import com.galilstay.server.dto.ExtraDto;
import com.galilstay.server.exception.NotFoundException;
import com.galilstay.server.model.Extra;
import com.galilstay.server.repository.ExtraRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtraService {

    private final ExtraRepository extras;
    private final ModelMapper mapper;

    public List<ExtraDto> findAll() {
        return extras.findAll().stream().map(e -> mapper.map(e, ExtraDto.class)).toList();
    }

    public ExtraDto create(ExtraDto dto) {
        Extra extra = mapper.map(dto, Extra.class);
        extra.setId(null);
        return mapper.map(extras.save(extra), ExtraDto.class);
    }

    public ExtraDto update(Long id, ExtraDto dto) {
        Extra extra = extras.findById(id).orElseThrow(() -> new NotFoundException("תוספת", id));
        extra.setTitle(dto.getTitle());
        extra.setPrice(dto.getPrice());
        extra.setChargedPerNight(dto.isChargedPerNight());
        return mapper.map(extras.save(extra), ExtraDto.class);
    }

    public void delete(Long id) {
        if (!extras.existsById(id)) throw new NotFoundException("תוספת", id);
        extras.deleteById(id);
    }

    List<Extra> loadAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Extra> found = extras.findAllById(ids);
        if (found.size() != ids.stream().distinct().count())
            throw new NotFoundException("תוספת", ids.get(0));
        return found;
    }
}
