package com.uco.ucopetapi.controllers.certificate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/certificate")
public class CertificateController {
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCertificates() {
        List<Map<String, Object>> spaces = List.of(
                Map.of(
                        "id", "e7b9a2c4-8d1e-4f3b-9a2c-1b2c3d4e5f6a",
                        "name", "Certificado cardiología",
                        "description", "Certificado correspondiente al postgrado de cardiología."
                ),
                Map.of(
                        "id", "e8b9a2c4-8d1e-4f3b-9a2c-1b2c3d4e5f6a",
                        "name", "Certificado pediatría",
                        "description", "Certificado correspondiente al postgrado de pediatría."
                )
        );
        return ResponseEntity.ok(spaces);
    }
}
