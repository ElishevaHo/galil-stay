package com.galilstay.server.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "יש להזין שם משתמש") String username,
        @NotBlank(message = "יש להזין סיסמה") String password
) {
}
