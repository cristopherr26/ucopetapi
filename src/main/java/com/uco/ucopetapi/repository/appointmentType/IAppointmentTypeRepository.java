package com.uco.ucopetapi.repository.appointmentType;

import com.uco.ucopetapi.domain.appointmentType.AppointmentTypeDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IAppointmentTypeRepository extends JpaRepository<AppointmentTypeDomain, UUID> {

    List<AppointmentTypeDomain> findByIsActiveTrue();
}