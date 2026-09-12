package com.uco.ucopetapi.service.certificate;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import com.uco.ucopetapi.repository.certificate.ICertificateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateService {

    private final ICertificateRepository iCertificateRepository;

    public CertificateService(ICertificateRepository iCertificateRepository) {
        this.iCertificateRepository = iCertificateRepository;
    }

    public List<CertificateDomain> getAllCertificates() {
        return iCertificateRepository.findAll();
    }
}
