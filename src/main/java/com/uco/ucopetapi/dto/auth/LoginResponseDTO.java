package com.uco.ucopetapi.dto.auth;

import java.util.UUID;


public record LoginResponseDTO(
        String token,
        UUID personId,
        String fullName,
        String role) {
}
