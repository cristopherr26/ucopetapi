package com.uco.ucopetapi.controllers.episode;


import com.uco.ucopetapi.dto.episode.DischargeType;
import com.uco.ucopetapi.dto.episode.EpisodeDto;
import com.uco.ucopetapi.dto.episode.EpisodeStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/episodes")
public class EpisodeController {

    private EpisodeDto buildEpisode(final UUID id, final UUID petId, final String description,
                                    final EpisodeStatus status, final DischargeType dischargeType) {
        return new EpisodeDto(
                id,
                "EP-" + id.toString().substring(0, 8).toUpperCase(),
                petId,
                description,
                LocalDateTime.of(2026, 8, 27, 9, 0),
                status == EpisodeStatus.DISCHARGED ? LocalDateTime.of(2026, 8, 27, 18, 0) : null,
                status,
                dischargeType,
                status == EpisodeStatus.DISCHARGED ? "Alta médica sin complicaciones." : null
        );
    }

    @GetMapping
    public ResponseEntity<List<EpisodeDto>> findAll() {
        List<EpisodeDto> episodes = List.of(
                buildEpisode(
                        UUID.fromString("c3a8d1e2-4b5f-4a6c-9d0e-1f2a3b4c5d6e"),
                        UUID.fromString("a1e6f0d2-8d1e-4f3b-9a2c-1b2c3d4e5f6a"),
                        "Consulta veterinaria general",
                        EpisodeStatus.ACTIVE,
                        null
                ),
                buildEpisode(
                        UUID.fromString("d4b9e2f3-5c6a-4b7d-8e1f-2a3b4c5d6e7f"),
                        UUID.fromString("b2f7e1d3-9e2f-5a4c-8b3d-2c3d4e5f6a7b"),
                        "Hospitalización por observación",
                        EpisodeStatus.DISCHARGED,
                        DischargeType.MEDICAL_DISCHARGE
                )
        );
        return ResponseEntity.ok(episodes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EpisodeDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildEpisode(
                id,
                UUID.fromString("a1e6f0d2-8d1e-4f3b-9a2c-1b2c3d4e5f6a"),
                "Consulta veterinaria general",
                EpisodeStatus.ACTIVE,
                null
        ));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<EpisodeDto>> findByFilter(
            @RequestParam(required = false) UUID pet,
            @RequestParam(required = false) EpisodeStatus episodeStatus,
            @RequestParam(required = false) String description) {

        UUID petId = pet != null ? pet : UUID.fromString("a1e6f0d2-8d1e-4f3b-9a2c-1b2c3d4e5f6a");
        EpisodeStatus status = episodeStatus != null ? episodeStatus : EpisodeStatus.ACTIVE;
        String filterDescription = description != null ? description : "Resultado filtrado";

        return ResponseEntity.ok(List.of(
                buildEpisode(UUID.randomUUID(), petId, filterDescription, status, null)
        ));
    }

    @PostMapping
    public ResponseEntity<EpisodeDto> createEpisode(@RequestBody EpisodeDto request) {
        UUID id = request.getId() != null ? request.getId() : UUID.randomUUID();
        EpisodeDto created = new EpisodeDto(
                id,
                request.getEpisodeNumber() != null
                        ? request.getEpisodeNumber()
                        : "EP-" + id.toString().substring(0, 8).toUpperCase(),
                request.getPet(),
                request.getDescription() != null ? request.getDescription() : "Episodio sin descripción",
                request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now(),
                request.getDischargeDate(),
                request.getEpisodeStatus() != null ? request.getEpisodeStatus() : EpisodeStatus.ACTIVE,
                request.getDischargeType(),
                request.getDischargeNotes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EpisodeDto> updateEpisodeById(
            @PathVariable UUID id,
            @RequestBody EpisodeDto request) {

        EpisodeDto updated = new EpisodeDto(
                id,
                request.getEpisodeNumber() != null
                        ? request.getEpisodeNumber()
                        : "EP-" + id.toString().substring(0, 8).toUpperCase(),
                request.getPet(),
                request.getDescription() != null ? request.getDescription() : "Episodio actualizado",
                request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now(),
                request.getDischargeDate(),
                request.getEpisodeStatus() != null ? request.getEpisodeStatus() : EpisodeStatus.ACTIVE,
                request.getDischargeType(),
                request.getDischargeNotes()
        );
        return ResponseEntity.ok(updated);
    }
}

