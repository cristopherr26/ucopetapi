package com.uco.ucopetapi.repository.certificate;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ICertificateRepository extends JpaRepository <CertificateDomain, UUID> {
}
