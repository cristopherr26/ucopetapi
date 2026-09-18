package com.uco.ucopetapi.domain.specialtie.mapper;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import com.uco.ucopetapi.domain.specialtie.SpecialtieDomain;
import com.uco.ucopetapi.dto.specialtie.SpecialtieDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpecialtieMapper {

    public SpecialtieDTO toDTO(SpecialtieDomain specialtieDomain){
        if (specialtieDomain == null) return null;

        return new SpecialtieDTO(
                specialtieDomain.getId(), specialtieDomain.getName(),
                specialtieDomain.isActive(),
                specialtieDomain.getDescription(), specialtieDomain.getCertificate().getId()
        );
    }

    public List<SpecialtieDTO> toDTOList(List<SpecialtieDomain> domains) {
        if (domains == null) return List.of();

        return domains.stream()
                .map(this::toDTO)
                .toList();
    }

    public SpecialtieDomain toDomain(SpecialtieDTO specialtieDTO){
        if (specialtieDTO == null) return null;

        CertificateDomain certificateDomain = new CertificateDomain();
        certificateDomain.setId(specialtieDTO.getCertificate());

        return new SpecialtieDomain(
                specialtieDTO.getId(), certificateDomain,
                specialtieDTO.getName(), specialtieDTO.isActive(),
                specialtieDTO.getDescription()
        );
    }
}
