package com.uco.ucopetapi.dto.tutor;

import java.time.LocalDateTime;
import java.util.UUID;

public record TutorDTO(
        UUID id,
        String documentNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        Boolean isActive,
        LocalDateTime deactivatedAt
) {
}
