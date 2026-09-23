package com.uco.ucopetapi.service.attention;

import java.util.UUID;

public final class CurrentDoctor {

    private final UUID doctorId;
    private final UUID personId;
    private final String fullName;
    private final String licenseNumber;

    public CurrentDoctor(final UUID doctorId, final UUID personId,
                         final String fullName, final String licenseNumber) {
        this.doctorId = doctorId;
        this.personId = personId;
        this.fullName = fullName;
        this.licenseNumber = licenseNumber;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }
}

