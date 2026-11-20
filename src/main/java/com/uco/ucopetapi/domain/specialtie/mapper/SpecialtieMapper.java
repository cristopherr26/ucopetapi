package com.uco.ucopetapi.domain.specialtie.mapper;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import com.uco.ucopetapi.domain.specialtie.SpecialtieDomain;
import com.uco.ucopetapi.dto.specialtie.SpecialtieDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SpecialtieMapper {

    public SpecialtieDTO toDTO(SpecialtieDomain specialtieDomain){
        if (specialtieDomain == null) return null;

        UUID certificateId = specialtieDomain.getCertificate() != null
                ? specialtieDomain.getCertificate().getId()
                : null;

        SpecialtieDTO dto = new SpecialtieDTO();
        dto.setId(specialtieDomain.getId());
        dto.setName(specialtieDomain.getName());
        dto.setDescription(specialtieDomain.getDescription());
        dto.setActive(specialtieDomain.isActive());
        dto.setCertificate(certificateId);
        return dto;
    }

    public List<SpecialtieDTO> toDTOList(List<SpecialtieDomain> domains) {
        if (domains == null) return List.of();

        return domains.stream()
                .map(this::toDTO)
                .toList();
    }

    public SpecialtieDomain toDomain(SpecialtieDTO specialtieDTO){
        if (specialtieDTO == null) return null;

        CertificateDomain certificateDomain = null;
        if (specialtieDTO.getCertificate() != null) {
            certificateDomain = new CertificateDomain();
            certificateDomain.setId(specialtieDTO.getCertificate());
        }

        boolean active = specialtieDTO.isActive() == null || specialtieDTO.isActive();

        return new SpecialtieDomain(
                specialtieDTO.getId(), certificateDomain,
                specialtieDTO.getName(), active,
                specialtieDTO.getDescription()
        );
    }
}
