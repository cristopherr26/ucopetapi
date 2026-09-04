package com.uco.ucopetapi.dto.episode;

import java.time.LocalDateTime;
import java.util.UUID;

public class EpisodeDto {
    private UUID id;
    private String episodeNumber;
    private UUID pet;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime dischargeDate;
    private EpisodeStatus episodeStatus;
    private DischargeType dischargeType;
    private String dischargeNotes;

    public EpisodeDto (final UUID id, final String episodeNumber, final UUID pet, final String description, final LocalDateTime startDate,
                       final LocalDateTime dischargeDate, final EpisodeStatus episodeStatus, final DischargeType dischargeType, final String dischargeNotes){
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

    public UUID getId() {
        return id;
    }

    public String getEpisodeNumber() {
        return episodeNumber;
    }

    public UUID getPet() {
        return pet;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public EpisodeStatus getEpisodeStatus() {
        return episodeStatus;
    }

    public DischargeType getDischargeType() {
        return dischargeType;
    }

    public String getDischargeNotes() {
        return dischargeNotes;
    }

    public void setEpisodeNumber(String episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public void setEpisodeStatus(EpisodeStatus episodeStatus) {
        this.episodeStatus = episodeStatus;
    }

    public void setDischargeType(DischargeType dischargeType) {
        this.dischargeType = dischargeType;
    }

    public void setDischargeNotes(String dischargeNotes) {
        this.dischargeNotes = dischargeNotes;
    }
}
