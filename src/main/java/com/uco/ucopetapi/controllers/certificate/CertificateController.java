package com.uco.ucopetapi.controllers.certificate;

import com.uco.ucopetapi.dto.certificate.CertificateDTO;
import com.uco.ucopetapi.service.certificate.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getAllCertificates() {

        List<CertificateDTO> dtos = certificateService.getAllCertificates();
        return ResponseEntity.ok(dtos);
    }
}

