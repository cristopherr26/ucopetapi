package com.uco.ucopetapi.domain.doctor.mapper;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.doctor.DoctorDTO;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorDTO toDTO(DoctorDomain doctor) {
        if (doctor == null) return null;
        return new DoctorDTO(
                doctor.getId(),
                doctor.getIdPerson(),
                doctor.getLicenseNumber());
    }

    public DoctorDomain toDomain(DoctorDTO doctor) {
        if (doctor == null) return null;
        return new DoctorDomain(
                doctor.getId(),
                doctor.getPerson(),
                doctor.getLicenseNumber());
    }
}