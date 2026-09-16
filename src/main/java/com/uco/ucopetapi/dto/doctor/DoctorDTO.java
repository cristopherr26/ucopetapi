package com.uco.ucopetapi.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class DoctorDTO {

    private UUID id;

    @NotNull(message = "El id de la persona es obligatorio")
    private UUID idPerson;

    @NotBlank(message = "El numero de licencia es obligatorio")
    private String licenseNumber;

    public DoctorDTO(){
    }

    public DoctorDTO(UUID id, UUID idPerson, String licenseNumber) {
        this.id = id;
        this.idPerson = idPerson;
        this.licenseNumber = licenseNumber;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // Nota: el getter/setter se llaman getPerson()/setPerson() (no getIdPerson()), asi que en el JSON esta propiedad aparece como
    // "person", no como "idPerson".

    public UUID getPerson() {
        return idPerson;
    }

    public void setPerson(UUID person) {
        this.idPerson = person;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}