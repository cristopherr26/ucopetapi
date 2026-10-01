package com.uco.ucopetapi.dto.medicationOrder;

import java.time.LocalDateTime;
import java.util.UUID;

public class MedicationOrderSignatureDTO {

    private UUID doctorId;
    private String doctorName;
    private String licenseNumber;
    private LocalDateTime signedAt;
    private String hash;

    public MedicationOrderSignatureDTO() {
    }

    public MedicationOrderSignatureDTO(final UUID doctorId, final String doctorName, final String licenseNumber,
                                       final LocalDateTime signedAt, final String hash) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.licenseNumber = licenseNumber;
        this.signedAt = signedAt;
        this.hash = hash;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}