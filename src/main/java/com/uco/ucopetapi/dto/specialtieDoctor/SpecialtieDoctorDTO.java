package com.uco.ucopetapi.dto.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.VerificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpecialtieDoctorDTO {

    private UUID id;
    private UUID idDoctor;
    private UUID idSpecialtie;
    private UUID headquarterId;
    private String certificateUrl;
    private String certificateNotes;
    private VerificationStatus status;
    private UUID verifiedBy;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    private Boolean active;

    // Solo constructor vacío a propósito: así Jackson 3 no elige un constructor con argumentos como creador
    // (sus nombres idDoctor/idSpecialtie no coinciden con las claves JSON "doctor"/"specialtie").
    public SpecialtieDoctorDTO() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDoctor() { return idDoctor; }
    public void setDoctor(UUID doctor) { this.idDoctor = doctor; }
    public UUID getSpecialtie() { return idSpecialtie; }
    public void setSpecialtie(UUID specialtie) { this.idSpecialtie = specialtie; }
    public UUID getHeadquarterId() { return headquarterId; }
    public void setHeadquarterId(UUID headquarterId) { this.headquarterId = headquarterId; }
    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
    public String getCertificateNotes() { return certificateNotes; }
    public void setCertificateNotes(String certificateNotes) { this.certificateNotes = certificateNotes; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public UUID getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(UUID verifiedBy) { this.verifiedBy = verifiedBy; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
