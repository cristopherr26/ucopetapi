package com.uco.ucopetapi.dto.specialtieDoctor;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SpecialtieDoctorDTO {

    private UUID id;

    @NotNull(message = "El id del doctor es obligatorio")
    private UUID idDoctor;

    @NotNull(message = "El id de la especialidad es obligatorio")
    private UUID idSpecialtie;
    public SpecialtieDoctorDTO() {
    }

    public SpecialtieDoctorDTO(UUID id, UUID idDoctor, UUID idSpecialtie) {
        this.id = id;
        this.idDoctor = idDoctor;
        this.idSpecialtie = idSpecialtie;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDoctor() {
        return idDoctor;
    }

    public void setDoctor(UUID doctor) {
        this.idDoctor = doctor;
    }

    public UUID getSpecialtie() {
        return idSpecialtie;
    }

    public void setSpecialtie(UUID specialtie) { this.idSpecialtie = specialtie; }

}