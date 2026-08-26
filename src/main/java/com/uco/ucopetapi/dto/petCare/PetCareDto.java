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
    private boolean petCateStatus;

    public PetCareDto (UUID id, UUID petsDto, UUID episodeDto, UUID procedureDto, UUID productDto, LocalDate attentionDate, String description, boolean petCareStatus){
        this.id = id;
        this.petsDto = petsDto;
        this.episodeDTO = episodeDto;
        this.procedureDTO = procedureDto;
        this.productDTO = productDto;
        this.attentionDate = attentionDate;
        this.description = description;
        this.petCateStatus= petCareStatus;
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

    public void setAttentionDate(LocalDate attentionDate) {
        this.attentionDate = attentionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPetCateStatus() {
        return petCateStatus;
    }

    public void setPetCateStatus(boolean petCateStatus) {
        this.petCateStatus = petCateStatus;
    }
}
