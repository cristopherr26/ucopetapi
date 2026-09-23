package com.uco.ucopetapi.domain.petCare;

import com.uco.ucopetapi.domain.vitalSigns.VitalSignsDomain;
import com.uco.ucopetapi.crosscutting.helpers.TextHelper;
import com.uco.ucopetapi.domain.clinical.ClinicalBaseDomain;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pet_cares")
public class PetCareDomain extends ClinicalBaseDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "episode_id", nullable = false)
    private UUID episodeId;

    @Column(name = "doctor_id")
    private UUID doctorId;

    @Column(name = "appointment_id", unique = true)
    private UUID appointmentId;

    @Column(name = "attention_date", nullable = false)
    private LocalDateTime attentionDate;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_care_status", nullable = false)
    private PetCareStatus petCareStatus;

    @OneToMany(mappedBy = "petCare", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VitalSignsDomain> vitalSigns = new ArrayList<>();

    public PetCareDomain() {
    }

    public PetCareDomain(final UUID id, final UUID episodeId,final UUID doctorId, final LocalDateTime attentionDate, final String description,
                         final PetCareStatus petCareStatus) {
        this.id = id;
        this.episodeId = episodeId;
        this.doctorId = doctorId;
        this.attentionDate = attentionDate;
        this.description = description;
        this.petCareStatus = petCareStatus;
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

    public void addVitalSign(final VitalSignsDomain vitalSign) {
        vitalSigns.add(vitalSign);
        vitalSign.setPetCare(this);
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

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
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

    public List<VitalSignsDomain> getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(List<VitalSignsDomain> vitalSigns) {
        this.vitalSigns = vitalSigns;
    }


    @Override
    public void validate() {
        requireNotNull(episodeId, "El episodio de la atención es obligatorio");
        requireNotNull(attentionDate, "La fecha de la atención es obligatoria");
        requireMaxLength(description, 1000, "La descripción de la atención no puede superar 1000 caracteres");
        if (vitalSigns != null) {
            for (VitalSignsDomain vitalSign : vitalSigns) {
                vitalSign.validate();
            }
        }
    }

    public boolean isOpen() {
        return petCareStatus == PetCareStatus.REGISTERED || petCareStatus == PetCareStatus.IN_PROGRESS;
    }

    public void ensureOpen() {
        requireState(isOpen(), "Solo se puede operar sobre una atención registrada o en curso");
    }

    public void complete(final UUID requestingDoctorId, final String summary) {
        ensureOpen();
        requireAuthor(doctorId != null && doctorId.equals(requestingDoctorId),
                "Solo el veterinario que inició la atención puede cerrarla");
        this.petCareStatus = PetCareStatus.COMPLETED;
        if (!TextHelper.getDefaultWithTrim(summary).isEmpty()) {
            String summaryLine = "Resumen: " + summary.trim();
            this.description = TextHelper.getDefaultWithTrim(description).isEmpty()
                    ? summaryLine
                    : description + " " + summaryLine;
        }
    }
}