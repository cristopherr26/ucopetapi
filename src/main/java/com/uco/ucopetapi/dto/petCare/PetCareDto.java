package com.uco.ucopetapi.dto.petCare;

import com.uco.ucopetapi.dto.vitalSigns.VitalSignsDTO;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PetCareDto {
    private UUID id;
    private UUID episodeId;
    private UUID doctorId;
    private UUID appointmentId;
    private LocalDateTime attentionDate;
    private String description;
    private PetCareStatus petCareStatus;
    private List<VitalSignsDTO> vitalSigns = new ArrayList<>();

    public PetCareDto() {
    }

    public PetCareDto (final UUID id, final UUID episodeId, final UUID doctorId, final LocalDateTime attentionDate,
                       final String description, final PetCareStatus petCareStatus, final List<VitalSignsDTO> vitalSigns){

        this.id = id;
        this.episodeId = episodeId;
        this.doctorId = doctorId;
        this.attentionDate = attentionDate;
        this.description = description;
        this.petCareStatus = petCareStatus;
        this.vitalSigns = vitalSigns != null ? vitalSigns : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
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
        return petCareStatus;
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

    public void setPetCareStatus(PetCareStatus petCareStatus) {
        this.petCareStatus = petCareStatus;
    }

    public void setVitalSigns(List<VitalSignsDTO> vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public void setEpisodeId(UUID episodeId) {
        this.episodeId = episodeId;
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
}
