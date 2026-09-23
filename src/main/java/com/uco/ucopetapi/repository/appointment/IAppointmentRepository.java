package com.uco.ucopetapi.repository.appointment;

import com.uco.ucopetapi.domain.appointment.AppointmentDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface IAppointmentRepository extends JpaRepository<AppointmentDomain, UUID> {

    List<AppointmentDomain> findByTutorId(UUID tutorId);

    List<AppointmentDomain> findByTutorIdAndStatus(UUID tutorId, String status);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(UUID doctorId,
                                                                 LocalDate appointmentDate,
                                                                 LocalTime appointmentTime);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNot(UUID doctorId,
                                                                         LocalDate appointmentDate,
                                                                         LocalTime appointmentTime,
                                                                         UUID id);
}