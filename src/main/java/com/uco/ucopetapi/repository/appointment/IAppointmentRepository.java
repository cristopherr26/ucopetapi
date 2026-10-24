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

    // Cruce de horario al CREAR una cita. Las citas con el estado indicado (CANCELLED)
    // no cuentan: una cita cancelada libera el horario del medico.
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(UUID doctorId,
                                                                             LocalDate appointmentDate,
                                                                             LocalTime appointmentTime,
                                                                             String status);

    // Cruce de horario al ACTUALIZAR: se ignora la propia cita y las canceladas
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNotAndStatusNot(UUID doctorId,
                                                                                     LocalDate appointmentDate,
                                                                                     LocalTime appointmentTime,
                                                                                     UUID id,
                                                                                     String status);
}
