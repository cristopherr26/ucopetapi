package com.uco.ucopetapi.controllers.certificate;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import com.uco.ucopetapi.dto.certificate.CertificateDTO;
import com.uco.ucopetapi.service.certificate.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getAllSpaces() {
        List<CertificateDomain> domains = certificateService.getAllCertificates();
        List<CertificateDTO> dtos = domains.stream()
                .map(d -> new CertificateDTO(d.getId(), d.getName(), d.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

