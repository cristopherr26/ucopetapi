package com.uco.ucopetapi.dto.person;

import java.util.List;
import java.util.UUID;

public record LoginResponseDTO(
        String token,
        UUID personId,
        String fullName,
        List<Role> roles) {
}
