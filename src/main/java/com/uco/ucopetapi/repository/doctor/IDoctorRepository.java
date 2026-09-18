package com.uco.ucopetapi.repository.doctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IDoctorRepository extends JpaRepository<DoctorDomain, UUID> {
    List<DoctorDomain> findByIdPerson(UUID idPerson);
    List<DoctorDomain> findByLicenseNumberContainingIgnoreCase(String licenseNumber);

    // Para validar persona duplicada y licencia duplicada en DoctorService.
    boolean existsByIdPerson(UUID idPerson);
    boolean existsByLicenseNumber(String licenseNumber);
}