package com.uco.ucopetapi.domain.specialtieDoctor.mapper;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.dto.specialtieDoctor.SpecialtieDoctorDTO;
import com.uco.ucopetapi.service.doctor.DoctorService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpecialtieDoctorMapper {

    private final DoctorService doctorService;

    public SpecialtieDoctorMapper(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    public SpecialtieDoctorDTO toDTO(SpecialtieDoctorDomain specialtieDoctor) {
        if (specialtieDoctor == null) return null;
        UUID doctorId = specialtieDoctor.getDoctor() != null ? specialtieDoctor.getDoctor().getId() : null;
        return new SpecialtieDoctorDTO(specialtieDoctor.getId(), doctorId, specialtieDoctor.getIdSpecialtie());
    }

    public SpecialtieDoctorDomain toDomain(SpecialtieDoctorDTO dto) {
        if (dto == null) return null;
        DoctorDomain doctor = dto.getDoctor() != null ? doctorService.findById(dto.getDoctor()) : null;
        return new SpecialtieDoctorDomain(dto.getId(), doctor, dto.getSpecialtie());
    }
}