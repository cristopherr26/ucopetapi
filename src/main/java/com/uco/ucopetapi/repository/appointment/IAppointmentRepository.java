package com.uco.ucopetapi.repository.appointment;

import com.uco.ucopetapi.domain.appointment.AppointmentDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IAppointmentRepository extends JpaRepository<AppointmentDomain, UUID> {

    List<AppointmentDomain> findByTutorId(UUID tutorId);

    List<AppointmentDomain> findByTutorIdAndStatus(UUID tutorId, String status);
}