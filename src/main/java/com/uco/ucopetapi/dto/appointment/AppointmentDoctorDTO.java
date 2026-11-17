package com.uco.ucopetapi.dto.appointment;

import java.util.UUID;

// El medico de la sesion: lo usa el frontend para no pedirle un dato que ya se sabe.
public record AppointmentDoctorDTO(UUID doctorId, UUID headquarterId) {
}
