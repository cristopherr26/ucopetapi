package com.uco.ucopetapi.controllers.episode;


import com.uco.ucopetapi.dto.episode.EpisodeDto;
import com.uco.ucopetapi.dto.episode.EpisodeStatus;
import com.uco.ucopetapi.service.episode.EpisodeService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/episodes")
public class EpisodeController {

    private final EpisodeService episodeService;

    public EpisodeController(EpisodeService episodeService) {
        this.episodeService = episodeService;
    }

    @GetMapping
    public ResponseEntity<List<EpisodeDto>> findAll() {
        return ResponseEntity.ok(episodeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EpisodeDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(episodeService.findById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<EpisodeDto>> findByFilter(
            @RequestParam(required = false) UUID pet,
            @RequestParam(required = false) EpisodeStatus episodeStatus,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(episodeService.findByFilter(pet, episodeStatus, description));
    }

    @PostMapping
    public ResponseEntity<EpisodeDto> createEpisode(@RequestBody EpisodeDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(episodeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EpisodeDto> updateEpisodeById(@PathVariable UUID id, @RequestBody EpisodeDto request) {
        return ResponseEntity.ok(episodeService.update(id, request));
    }
}

