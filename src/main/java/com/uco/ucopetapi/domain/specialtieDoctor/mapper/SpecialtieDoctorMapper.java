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
        SpecialtieDoctorDTO dto = new SpecialtieDoctorDTO();
        dto.setId(specialtieDoctor.getId());
        dto.setDoctor(doctorId);
        dto.setSpecialtie(specialtieDoctor.getIdSpecialtie());
        dto.setHeadquarterId(specialtieDoctor.getHeadquarterId());
        dto.setCertificateUrl(specialtieDoctor.getCertificateUrl());
        dto.setCertificateNotes(specialtieDoctor.getCertificateNotes());
        dto.setStatus(specialtieDoctor.getStatus());
        dto.setVerifiedBy(specialtieDoctor.getVerifiedBy());
        dto.setVerifiedAt(specialtieDoctor.getVerifiedAt());
        dto.setRejectionReason(specialtieDoctor.getRejectionReason());
        dto.setActive(specialtieDoctor.isActive());
        return dto;
    }

    /**
     * Solo copia los campos que el cliente puede enviar. status, verifiedBy, verifiedAt,
     * rejectionReason y active los controla el servicio.
     */
    public SpecialtieDoctorDomain toDomain(SpecialtieDoctorDTO dto) {
        if (dto == null) return null;
        DoctorDomain doctor = dto.getDoctor() != null ? doctorService.findById(dto.getDoctor()) : null;
        SpecialtieDoctorDomain domain =
                new SpecialtieDoctorDomain(dto.getId(), doctor, dto.getSpecialtie(), dto.getHeadquarterId());
        domain.setCertificateUrl(dto.getCertificateUrl());
        domain.setCertificateNotes(dto.getCertificateNotes());
        return domain;
    }
}
