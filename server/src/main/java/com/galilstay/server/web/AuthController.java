package com.galilstay.server.web;

import com.galilstay.server.dto.HostDto;
import com.galilstay.server.dto.LoginRequest;
import com.galilstay.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public HostDto login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
