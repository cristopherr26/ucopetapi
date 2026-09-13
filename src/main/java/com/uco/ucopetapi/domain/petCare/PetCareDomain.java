package com.uco.ucopetapi.domain.petCare;

import com.uco.ucopetapi.domain.vitalSigns.VitalSignsDomain;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pet_cares")
public class PetCareDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Column(name = "procedure_id")
    private UUID procedureId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "doctor_id")
    private UUID doctorId;

    @Column(name = "attention_date", nullable = false)
    private LocalDateTime attentionDate;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_care_status", nullable = false)
    private PetCareStatus petCareStatus;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "vital_signs_id")
    private VitalSignsDomain vitalSigns;

    public PetCareDomain() {
    }

    public PetCareDomain(final UUID id, final UUID episodeId, final UUID procedureId, final UUID productId,
                         final UUID doctorId, final LocalDateTime attentionDate, final String description,
                         final PetCareStatus petCareStatus, final VitalSignsDomain vitalSigns) {
        this.id = id;
        this.episodeId = episodeId;
        this.procedureId = procedureId;
        this.productId = productId;
        this.doctorId = doctorId;
        this.attentionDate = attentionDate;
        this.description = description;
        this.petCareStatus = petCareStatus;
        this.vitalSigns = vitalSigns;
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (attentionDate == null) {
            attentionDate = LocalDateTime.now();
        }
        if (petCareStatus == null) {
            petCareStatus = PetCareStatus.REGISTERED;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(UUID episodeId) {
        this.episodeId = episodeId;
    }

    public UUID getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(UUID procedureId) {
        this.procedureId = procedureId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAttentionDate() {
        return attentionDate;
    }

    public void setAttentionDate(LocalDateTime attentionDate) {
        this.attentionDate = attentionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PetCareStatus getPetCareStatus() {
        return petCareStatus;
    }

    public void setPetCareStatus(PetCareStatus petCareStatus) {
        this.petCareStatus = petCareStatus;
    }

    public VitalSignsDomain getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(VitalSignsDomain vitalSigns) {
        this.vitalSigns = vitalSigns;
    }
}