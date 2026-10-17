package com.uco.ucopetapi.dto.attention;

import com.uco.ucopetapi.dto.episode.EpisodeDto;
import com.uco.ucopetapi.dto.petCare.PetCareDto;

import java.util.UUID;

public class AttentionResponse {

    private UUID appointmentId;
    private String appointmentStatus;
    private EpisodeDto episode;
    private PetCareDto petCare;

    public AttentionResponse() {
    }

    public AttentionResponse(final UUID appointmentId, final String appointmentStatus,
                             final EpisodeDto episode, final PetCareDto petCare) {
        this.appointmentId = appointmentId;
        this.appointmentStatus = appointmentStatus;
        this.episode = episode;
        this.petCare = petCare;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public EpisodeDto getEpisode() {
        return episode;
    }

    public void setEpisode(EpisodeDto episode) {
        this.episode = episode;
    }

    public PetCareDto getPetCare() {
        return petCare;
    }

    public void setPetCare(PetCareDto petCare) {
        this.petCare = petCare;
    }
}
