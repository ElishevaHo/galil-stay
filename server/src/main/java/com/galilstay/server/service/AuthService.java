package com.galilstay.server.service;

import com.galilstay.server.dto.HostDto;
import com.galilstay.server.dto.LoginRequest;
import com.galilstay.server.exception.BadLoginException;
import com.galilstay.server.model.Host;
import com.galilstay.server.repository.HostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final HostRepository hosts;
    private final PasswordEncoder encoder;

    public HostDto login(LoginRequest request) {
        Host host = hosts.findByUsername(request.username().trim())
                .filter(h -> encoder.matches(request.password(), h.getPasswordHash()))
                .orElseThrow(BadLoginException::new);
        return new HostDto(host.getId(), host.getUsername(), host.getDisplayName());
    }
}
