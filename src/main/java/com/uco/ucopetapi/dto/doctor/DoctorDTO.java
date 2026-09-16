package com.uco.ucopetapi.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class DoctorDTO {

    private UUID id;

    @NotNull(message = "El id de la persona es obligatorio")
    private UUID idPerson;

    @NotBlank(message = "El numero de licencia es obligatorio")
    @Size(min = 4, max = 30, message = "El numero de licencia debe tener entre 4 y 30 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El numero de licencia solo puede contener letras, numeros y guiones")
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