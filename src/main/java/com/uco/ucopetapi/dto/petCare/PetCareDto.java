package com.uco.ucopetapi.dto.petCare;

import java.time.LocalDate;

import java.util.UUID;

public class PetCareDto {
    private UUID id;
    private UUID petsDto;
    private UUID episodeDTO;
    private UUID procedureDTO;
    private UUID productDTO;
    private LocalDate attentionDate;
    private String description;
    private boolean isPetCateStatus;

    public PetCareDto (final UUID id, final UUID petsDto, final UUID episodeDto, final UUID procedureDto, final UUID productDto, final LocalDate attentionDate, final String description, final boolean isPetCareStatus){
        this.id = id;
        this.petsDto = petsDto;
        this.episodeDTO = episodeDto;
        this.procedureDTO = procedureDto;
        this.productDTO = productDto;
        this.attentionDate = attentionDate;
        this.description = description;
        this.isPetCateStatus= isPetCareStatus;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPetsDto() {
        return petsDto;
    }

    public UUID getEpisodeDTO() {
        return episodeDTO;
    }

    public UUID getProcedureDTO() {
        return procedureDTO;
    }

    public UUID getProductDTO() {
        return productDTO;
    }

    public LocalDate getAttentionDate() {
        return attentionDate;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPetCateStatus() {
        return isPetCateStatus;
    }

    public void setAttentionDate(LocalDate attentionDate) {
        this.attentionDate = attentionDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPetCateStatus(boolean isPetCateStatus) {
        this.isPetCateStatus = isPetCateStatus;
    }
}
