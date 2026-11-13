package com.uco.ucopetapi.dto.specialtieDoctor;

import java.util.UUID;

public class SpecialtieDoctorDTO {

    private UUID id;
    private UUID idDoctor;
    private UUID idSpecialtie;
    private UUID headquarterId;

    public SpecialtieDoctorDTO() {
    }

    public SpecialtieDoctorDTO(UUID id, UUID idDoctor, UUID idSpecialtie, UUID headquarterId) {
        this.id = id;
        this.idDoctor = idDoctor;
        this.idSpecialtie = idSpecialtie;
        this.headquarterId = headquarterId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDoctor() { return idDoctor; }
    public void setDoctor(UUID doctor) { this.idDoctor = doctor; }
    public UUID getSpecialtie() { return idSpecialtie; }
    public void setSpecialtie(UUID specialtie) { this.idSpecialtie = specialtie; }
    public UUID getHeadquarterId() { return headquarterId; }
    public void setHeadquarterId(UUID headquarterId) { this.headquarterId = headquarterId; }
}