package com.uco.ucopetapi.dto.doctor;

import java.util.UUID;

public class DoctorDTO {

    private UUID id;
    private UUID idPerson;
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