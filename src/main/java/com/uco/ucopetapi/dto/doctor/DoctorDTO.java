package com.uco.ucopetapi.dto.doctor;

import com.uco.ucopetapi.dto.person.PersonDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public class DoctorDTO {

    private UUID id;
    private PersonDTO person;
    private String licenseNumber;
    private String address;
    private LocalDateTime deactivatedAt;


    public DoctorDTO(){
    }

    public DoctorDTO(UUID id, PersonDTO person, String licenseNumber, String address, LocalDateTime deactivatedAt) {
        this.id = id;
        this.person = person;
        this.licenseNumber = licenseNumber;
        this.address = address;
        this.deactivatedAt = deactivatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(PersonDTO person) {
        this.person = person;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(LocalDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }
}