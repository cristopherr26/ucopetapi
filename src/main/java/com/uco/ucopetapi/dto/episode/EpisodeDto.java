package com.uco.ucopetapi.dto.episode;

import java.time.LocalDate;
import java.util.UUID;

public class EpisodeDto {
    private UUID id;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isStatus;

    public EpisodeDto (final UUID id,final String description,final LocalDate startDate,final LocalDate endDate, boolean isStatus){
        this.id = id;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isStatus=isStatus;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isStatus() {
        return isStatus;
    }

    public void setStatus(boolean isStatus) {
        this.isStatus = isStatus;
    }
}
