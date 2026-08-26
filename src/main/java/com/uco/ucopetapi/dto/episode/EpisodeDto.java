package com.uco.ucopetapi.dto.episode;

import java.time.LocalDate;
import java.util.UUID;

public class EpisodeDto {
    private UUID id;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean status;

    public EpisodeDto (final UUID id,final String description,final LocalDate starDate,final LocalDate endDate, boolean status){
        this.id = id;
        this.description = description;
        this.startDate = starDate;
        this.endDate = endDate;
        this.status=status;
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
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
