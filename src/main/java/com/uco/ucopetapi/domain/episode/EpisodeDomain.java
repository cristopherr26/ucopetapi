package com.uco.ucopetapi.domain.episode;

import com.uco.ucopetapi.domain.clinical.ClinicalBaseDomain;
import com.uco.ucopetapi.dto.episode.DischargeType;
import com.uco.ucopetapi.dto.episode.EpisodeStatus;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "episodes")
public class EpisodeDomain extends ClinicalBaseDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "episode_number", nullable = false, unique = true)
    private String episodeNumber;

    @Column(name = "pet_id", nullable = false)
    private UUID pet;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "episode_status", nullable = false)
    private EpisodeStatus episodeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_type")
    private DischargeType dischargeType;

    @Column(name = "discharge_notes")
    private String dischargeNotes;

    public EpisodeDomain() {
    }

    public EpisodeDomain(final UUID id, final String episodeNumber, final UUID pet, final String description,
                   final LocalDateTime startDate, final LocalDateTime dischargeDate,
                   final EpisodeStatus episodeStatus, final DischargeType dischargeType,
                   final String dischargeNotes) {
        this.id = id;
        this.episodeNumber = episodeNumber;
        this.pet = pet;
        this.description = description;
        this.startDate = startDate;
        this.dischargeDate = dischargeDate;
        this.episodeStatus = episodeStatus;
        this.dischargeType = dischargeType;
        this.dischargeNotes = dischargeNotes;
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (episodeNumber == null) {
            episodeNumber = "EP-" + id.toString().substring(0, 8).toUpperCase();
        }
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (episodeStatus == null) {
            episodeStatus = EpisodeStatus.ACTIVE;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(String episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public UUID getPet() {
        return pet;
    }

    public void setPet(UUID pet) {
        this.pet = pet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public EpisodeStatus getEpisodeStatus() {
        return episodeStatus;
    }

    public void setEpisodeStatus(EpisodeStatus episodeStatus) {
        this.episodeStatus = episodeStatus;
    }

    public DischargeType getDischargeType() {
        return dischargeType;
    }

    public void setDischargeType(DischargeType dischargeType) {
        this.dischargeType = dischargeType;
    }

    public String getDischargeNotes() {
        return dischargeNotes;
    }

    public void setDischargeNotes(String dischargeNotes) {
        this.dischargeNotes = dischargeNotes;
    }

    @Override
    public void validate() {
        requireNotNull(pet, "La mascota del episodio es obligatoria");
        requireNotNull(startDate, "La fecha de inicio del episodio es obligatoria");
        requireMaxLength(description, 500, "La descripción del episodio no puede superar 500 caracteres");
        if (episodeStatus == EpisodeStatus.DISCHARGED) {
            requireNotNull(dischargeType, "El tipo de alta es obligatorio para cerrar el episodio");
            requireNotNull(dischargeDate, "La fecha de alta es obligatoria");
            if (dischargeDate.isBefore(startDate)) {
                throw ClinicalException.badRequest(
                        "La fecha de alta no puede ser anterior a la fecha de inicio");
            }
        }
    }

    public boolean isOpen() {
        return episodeStatus == EpisodeStatus.ACTIVE
                || episodeStatus == EpisodeStatus.IN_OBSERVATION
                || episodeStatus == EpisodeStatus.HOSPITALIZED;
    }

    public void discharge(final DischargeType type, final String notes, final LocalDateTime date) {
        requireState(isOpen(), "Solo se puede dar de alta un episodio abierto");
        requireNotNull(type, "El tipo de alta es obligatorio para cerrar el episodio");
        this.episodeStatus = EpisodeStatus.DISCHARGED;
        this.dischargeType = type;
        this.dischargeDate = date;
        this.dischargeNotes = notes;
    }
}
