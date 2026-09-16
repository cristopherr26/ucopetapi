package com.uco.ucopetapi.service.episode;

import com.uco.ucopetapi.dto.episode.EpisodeDto;
import com.uco.ucopetapi.dto.episode.EpisodeStatus;

import java.util.List;
import java.util.UUID;

public interface EpisodeService {

    List<EpisodeDto> findAll();

    EpisodeDto findById(UUID id);

    List<EpisodeDto> findByFilter(UUID pet, EpisodeStatus episodeStatus, String description);

    EpisodeDto create(EpisodeDto request);

    EpisodeDto update(UUID id, EpisodeDto request);
}
