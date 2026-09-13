package com.uco.ucopetapi.repository.petCare;

import com.uco.ucopetapi.domain.petCare.PetCareDomain;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PetCareRepository extends JpaRepository <PetCareDomain, UUID> {

    List<PetCareDomain> findByEpisodeId(UUID episodeId);

    List<PetCareDomain> findByPetCareStatus(PetCareStatus petCareStatus);

    List<PetCareDomain> findByDescriptionContainingIgnoreCase(String description);
}
