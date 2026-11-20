package com.uco.ucopetapi.domain.specialtieDoctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "specialtie_doctors")
public class SpecialtieDoctorDomain {

    public static final int CERTIFICATE_URL_MAX_LENGTH = 2048;
    public static final int TEXT_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doctor", nullable = false)
    private DoctorDomain doctor;

    @Column(name = "id_specialtie", nullable = false)
    private UUID idSpecialtie;

    @Column(name = "headquarter_id", nullable = false)
    private UUID headquarterId;

    // Columnas nuevas: nullable en BD porque el esquema lo actualiza Hibernate (ddl-auto=update)
    // y ya existen filas. Los nulos se interpretan en los getters (status -> PENDING, isActive -> true).
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private VerificationStatus status;

    @Column(name = "certificate_url", length = CERTIFICATE_URL_MAX_LENGTH)
    private String certificateUrl;

    @Column(name = "certificate_notes", length = TEXT_MAX_LENGTH)
    private String certificateNotes;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", length = TEXT_MAX_LENGTH)
    private String rejectionReason;

    @Column(name = "is_active")
    private Boolean isActive;

    public SpecialtieDoctorDomain() {
        this.status = VerificationStatus.PENDING;
        this.isActive = Boolean.TRUE;
    }

    public SpecialtieDoctorDomain(UUID id, DoctorDomain doctor, UUID idSpecialtie, UUID headquarterId) {
        this();
        this.id = id;
        this.doctor = doctor;
        this.idSpecialtie = idSpecialtie;
        this.headquarterId = headquarterId;
    }

    @PrePersist
    void applyDefaults() {
        if (status == null) {
            status = VerificationStatus.PENDING;
        }
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DoctorDomain getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDomain doctor) {
        this.doctor = doctor;
    }

    public UUID getIdSpecialtie() {
        return idSpecialtie;
    }

    public void setIdSpecialtie(UUID idSpecialtie) {
        this.idSpecialtie = idSpecialtie;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public void setHeadquarterId(UUID headquarterId) {
        this.headquarterId = headquarterId;
    }

    /** Una fila existente sin valor se trata como PENDING. */
    public VerificationStatus getStatus() {
        return status == null ? VerificationStatus.PENDING : status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public String getCertificateNotes() {
        return certificateNotes;
    }

    public void setCertificateNotes(String certificateNotes) {
        this.certificateNotes = certificateNotes;
    }

    public UUID getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(UUID verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    /** Una fila existente sin valor se trata como activa. */
    public boolean isActive() {
        return !Boolean.FALSE.equals(isActive);
    }

    public void setActive(Boolean active) {
        this.isActive = active;
    }
}
