package com.uco.ucopetapi.domain.certificate.mapper;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import com.uco.ucopetapi.dto.certificate.CertificateDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CertificateMapper {

    public CertificateDTO toDTO(CertificateDomain certificateDomain){
        if (certificateDomain == null) return null;

        return new CertificateDTO(
                certificateDomain.getId(), certificateDomain.getName(),
                certificateDomain.getDescription()
        );
    }

    public List<CertificateDTO> toDTOList(List<CertificateDomain> domains) {
        if (domains == null) return List.of();

        return domains.stream()
                .map(this::toDTO)
                .toList();
    }
}
