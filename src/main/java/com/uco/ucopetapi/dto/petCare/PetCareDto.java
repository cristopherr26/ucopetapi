package com.uco.ucopetapi.dto.petCare;

import com.uco.ucopetapi.dto.vitalSigns.VitalSignsDTO;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PetCareDto {
    private UUID id;
    private UUID episodeId;
    private UUID procedureId;
    private UUID productId;
    private UUID doctorId;
    private LocalDateTime attentionDate;
    private String description;
    private PetCareStatus petCateStatus;
    private List<VitalSignsDTO> vitalSigns = new ArrayList<>();

    public PetCareDto() {
    }

    public PetCareDto (final UUID id, final UUID episodeId, final UUID procedureId,
                       final UUID productId, final UUID doctorId, final LocalDateTime attentionDate,
                       final String description, final PetCareStatus petCateStatus, final List<VitalSignsDTO> vitalSigns){

        this.id = id;
        this.episodeId = episodeId;
        this.procedureId = procedureId;
        this.productId = productId;
        this.doctorId = doctorId;
        this.attentionDate = attentionDate;
        this.description = description;
        this.petCateStatus = petCateStatus;
        this.vitalSigns = vitalSigns != null ? vitalSigns : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public UUID getProcedureId() {
        return procedureId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public LocalDateTime getAttentionDate() {
        return attentionDate;
    }

    public String getDescription() {
        return description;
    }

    public PetCareStatus getPetCareStatus() {
        return petCateStatus;
    }

    public List<VitalSignsDTO>  getVitalSigns() {
        return vitalSigns;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAttentionDate(LocalDateTime attentionDate) {
        this.attentionDate = attentionDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPetCateStatus(PetCareStatus petCateStatus) {
        this.petCateStatus = petCateStatus;
    }

    public void setVitalSigns(List<VitalSignsDTO> vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public void setEpisodeId(UUID episodeId) {
        this.episodeId = episodeId;
    }

    public void setProcedureId(UUID procedureId) {
        this.procedureId = procedureId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }
}
