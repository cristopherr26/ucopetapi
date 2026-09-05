package com.uco.ucopetapi.domain.doctor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "doctors")
public class DoctorDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "id_person", nullable = false)
    private UUID idPerson;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;


    public DoctorDomain() {
    }

    public DoctorDomain(UUID id, UUID idPerson, String licenseNumber) {
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

    public UUID getIdPerson() {
        return idPerson;
    }

    public void setIdPerson(UUID idPerson) {
        this.idPerson = idPerson;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
}