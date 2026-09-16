package com.uco.ucopetapi.service.episode;


import com.uco.ucopetapi.domain.episode.EpisodeDomain;
import com.uco.ucopetapi.dto.episode.EpisodeDto;
import com.uco.ucopetapi.dto.episode.EpisodeStatus;
import com.uco.ucopetapi.repository.episode.EpisodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class EpisodeServiceImpl implements EpisodeService {

    private final EpisodeRepository episodeRepository;

    public EpisodeServiceImpl(final EpisodeRepository episodeRepository) {
        this.episodeRepository = episodeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpisodeDto> findAll() {
        return episodeRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeDto findById(final UUID id) {
        return episodeRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Episodio no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpisodeDto> findByFilter(final UUID pet, final EpisodeStatus episodeStatus, final String description) {
        String normalizedDescription = blankToNull(description);
        List<EpisodeDomain> episodes;

        if (pet != null && episodeStatus == null && normalizedDescription == null) {
            episodes = episodeRepository.findByPet(pet);
        } else if (pet == null && episodeStatus != null && normalizedDescription == null) {
            episodes = episodeRepository.findByEpisodeStatus(episodeStatus);
        } else if (pet == null && episodeStatus == null && normalizedDescription != null) {
            episodes = episodeRepository.findByDescriptionContainingIgnoreCase(normalizedDescription);
        } else if (pet == null && episodeStatus == null) {
            episodes = episodeRepository.findAll();
        } else {
            episodes = episodeRepository.findAll().stream()
                    .filter(episode -> pet == null || pet.equals(episode.getPet()))
                    .filter(episode -> episodeStatus == null || episodeStatus == episode.getEpisodeStatus())
                    .filter(episode -> normalizedDescription == null
                            || (episode.getDescription() != null
                            && episode.getDescription().toLowerCase().contains(normalizedDescription.toLowerCase())))
                    .toList();
        }

        return episodes.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EpisodeDto create(final EpisodeDto request) {
        EpisodeDomain episode = toEntity(request, request.getId() != null ? request.getId() : UUID.randomUUID());
        return toDto(episodeRepository.save(episode));
    }

    @Override
    @Transactional
    public EpisodeDto update(final UUID id, final EpisodeDto request) {
        EpisodeDomain episode = episodeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Episodio no encontrado: " + id));

        if (request.getEpisodeNumber() != null) {
            episode.setEpisodeNumber(request.getEpisodeNumber());
        }
        if (request.getPet() != null) {
            episode.setPet(request.getPet());
        }
        if (request.getDescription() != null) {
            episode.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            episode.setStartDate(request.getStartDate());
        }
        if (request.getDischargeDate() != null) {
            episode.setDischargeDate(request.getDischargeDate());
        }
        if (request.getEpisodeStatus() != null) {
            episode.setEpisodeStatus(request.getEpisodeStatus());
        }
        if (request.getDischargeType() != null) {
            episode.setDischargeType(request.getDischargeType());
        }
        if (request.getDischargeNotes() != null) {
            episode.setDischargeNotes(request.getDischargeNotes());
        }

        return toDto(episodeRepository.save(episode));
    }

    private EpisodeDto toDto(final EpisodeDomain episode) {
        return new EpisodeDto(
                episode.getId(),
                episode.getEpisodeNumber(),
                episode.getPet(),
                episode.getDescription(),
                episode.getStartDate(),
                episode.getDischargeDate(),
                episode.getEpisodeStatus(),
                episode.getDischargeType(),
                episode.getDischargeNotes()
        );
    }

    private EpisodeDomain toEntity(final EpisodeDto request, final UUID id) {
        String episodeNumber = request.getEpisodeNumber() != null
                ? request.getEpisodeNumber()
                : "EP-" + id.toString().substring(0, 8).toUpperCase();

        return new EpisodeDomain(
                id,
                episodeNumber,
                request.getPet(),
                request.getDescription() != null ? request.getDescription() : "Episodio sin descripción",
                request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now(),
                request.getDischargeDate(),
                request.getEpisodeStatus() != null ? request.getEpisodeStatus() : EpisodeStatus.ACTIVE,
                request.getDischargeType(),
                request.getDischargeNotes()
        );
    }

    private String blankToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
