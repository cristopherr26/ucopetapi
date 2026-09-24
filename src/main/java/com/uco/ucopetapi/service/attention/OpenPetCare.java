package com.uco.ucopetapi.service.attention;

import java.util.UUID;

public final class OpenPetCare {

    private final UUID petCareId;
    private final UUID episodeId;
    private final UUID petId;
    private final UUID doctorId;

    public OpenPetCare(final UUID petCareId, final UUID episodeId, final UUID petId, final UUID doctorId) {
        this.petCareId = petCareId;
        this.episodeId = episodeId;
        this.petId = petId;
        this.doctorId = doctorId;
    }

    public UUID getPetCareId() {
        return petCareId;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public UUID getPetId() {
        return petId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }
}
