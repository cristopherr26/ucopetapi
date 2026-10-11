package com.uco.ucopetapi.service.attention;

import com.uco.ucopetapi.domain.episode.EpisodeDomain;
import com.uco.ucopetapi.domain.petCare.PetCareDomain;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import com.uco.ucopetapi.repository.episode.EpisodeRepository;
import com.uco.ucopetapi.repository.petCare.PetCareRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OpenPetCareLoader {

    private final PetCareRepository petCareRepository;
    private final EpisodeRepository episodeRepository;

    public OpenPetCareLoader(final PetCareRepository petCareRepository, final EpisodeRepository episodeRepository) {
        this.petCareRepository = petCareRepository;
        this.episodeRepository = episodeRepository;
    }

    public OpenPetCare load(final UUID petCareId) {
        PetCareDomain petCare = petCareRepository.findById(petCareId)
                .orElseThrow(() -> ClinicalException.notFound("No se encontró la atención indicada"));
        petCare.ensureOpen();

        EpisodeDomain episode = episodeRepository.findById(petCare.getEpisodeId())
                .orElseThrow(() -> ClinicalException.notFound("No se encontró el episodio de la atención"));
        return new OpenPetCare(petCare.getId(), episode.getId(), episode.getPet(), petCare.getDoctorId());
    }

    public void ensureExists(final UUID petCareId) {
        if (petCareId == null || !petCareRepository.existsById(petCareId)) {
            throw ClinicalException.notFound("No se encontró la atención indicada");
        }
    }
}

