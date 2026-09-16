package com.uco.ucopetapi.repository.episode;

import com.uco.ucopetapi.domain.episode.EpisodeDomain;
import com.uco.ucopetapi.dto.episode.EpisodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EpisodeRepository extends JpaRepository<EpisodeDomain, UUID> {


    List<EpisodeDomain> findByPet(UUID pet);

    List<EpisodeDomain> findByEpisodeStatus(EpisodeStatus episodeStatus);

    List<EpisodeDomain> findByDescriptionContainingIgnoreCase(String description);
}
