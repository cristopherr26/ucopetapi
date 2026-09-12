package com.uco.ucopetapi.event;

import java.util.UUID;

public class AppointmentCreatedEvent {

    private final UUID appointmentId;
    private final UUID personId;
    private final String petName;

    public AppointmentCreatedEvent(UUID appointmentId, UUID personId, String petName) {
        this.appointmentId = appointmentId;
        this.personId = personId;
        this.petName = petName;
    }

    public UUID getAppointmentId() { return appointmentId; }
    public UUID getPersonId() { return personId; }
    public String getPetName() { return petName; }
}
