package com.uco.ucopetapi.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class AppointmentCreatedEvent {

    private final UUID appointmentId;
    private final UUID doctorId;
    private final String petName;
    private final LocalDate date;
    private final LocalTime time;

    public AppointmentCreatedEvent(UUID appointmentId, UUID doctorId, String petName, LocalDate date, LocalTime time) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.petName = petName;
        this.date = date;
        this.time = time;
    }

    public UUID getAppointmentId() { return appointmentId; }
    public UUID getDoctorId() { return doctorId; }
    public String getPetName() { return petName; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
}
