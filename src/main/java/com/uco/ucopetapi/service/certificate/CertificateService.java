package com.uco.ucopetapi.service.certificate;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import com.uco.ucopetapi.domain.certificate.mapper.CertificateMapper;
import com.uco.ucopetapi.dto.certificate.CertificateDTO;
import com.uco.ucopetapi.repository.certificate.ICertificateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateService {

    private final ICertificateRepository iCertificateRepository;
    private final CertificateMapper certificateMapper;

    public CertificateService(ICertificateRepository iCertificateRepository, CertificateMapper certificateMapper) {
        this.iCertificateRepository = iCertificateRepository;
        this.certificateMapper = certificateMapper;
    }

    public List<CertificateDTO> getAllCertificates() {
        List<CertificateDomain> domains = iCertificateRepository.findAll();
        return certificateMapper.toDTOList(domains);
    }
}
