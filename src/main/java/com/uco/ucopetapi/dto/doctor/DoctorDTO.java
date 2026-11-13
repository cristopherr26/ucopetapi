package com.uco.ucopetapi.dto.doctor;

import java.util.UUID;

public class DoctorDTO {

    private UUID id;
    private UUID idPerson;
    private String licenseNumber;
    private UUID headquarterId;

    public DoctorDTO() {
    }

    public DoctorDTO(UUID id, UUID idPerson, String licenseNumber, UUID headquarterId) {
        this.id = id;
        this.idPerson = idPerson;
        this.licenseNumber = licenseNumber;
        this.headquarterId = headquarterId;
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

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public void setHeadquarterId(UUID headquarterId) {
        this.headquarterId = headquarterId;
    }
}