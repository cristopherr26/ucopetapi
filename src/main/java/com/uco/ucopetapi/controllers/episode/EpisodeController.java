package com.uco.ucopetapi.controllers.episode;


import com.uco.ucopetapi.dto.episode.EpisodeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("ucopet/api/v1/")

public class EpisodeController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> findAll() {

        List<Map<String, Object>> episodes = List.of(
                Map.of(
                        "id", UUID.randomUUID().toString(),
                        "description", "Consulta veterinaria general",
                        "startDate", "2026-08-27",
                        "endDate", "2026-08-27",
                        "isStatus", true
                ),
                Map.of(
                        "id", UUID.randomUUID().toString(),
                        "description", "Tratamiento y seguimiento",
                        "startDate", "2026-08-20",
                        "endDate", "2026-08-25",
                        "isStatus", false
                )
        );

        return ResponseEntity.ok(episodes);
    }

    // FIND BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(
            @PathVariable String id) {

        Map<String, Object> episode = Map.of(
                "id", id,
                "description", "Consulta veterinaria general",
                "startDate", "2026-08-27",
                "endDate", "2026-08-27",
                "isStatus", true
        );

        return ResponseEntity.ok(episode);
    }

    // FIND BY FILTER
    @GetMapping("/filter")
    public ResponseEntity<List<Map<String, Object>>> findByFilter(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isStatus) {

        List<Map<String, Object>> episodes = List.of(
                Map.of(
                        "id", UUID.randomUUID().toString(),
                        "description", description != null
                                ? description
                                : "Resultado filtrado",
                        "startDate", "2026-08-27",
                        "endDate", "2026-08-27",
                        "isStatus", isStatus != null ? isStatus : true
                )
        );

        return ResponseEntity.ok(episodes);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEpisode(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", UUID.randomUUID().toString(),
                "description", request.getOrDefault(
                        "description", "Episodio sin descripción"),
                "startDate", request.getOrDefault(
                        "startDate", "2026-08-27"),
                "endDate", request.getOrDefault(
                        "endDate", "2026-08-27"),
                "isStatus", request.getOrDefault("isStatus", true)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEpisodeById(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", id,
                "description", request.getOrDefault(
                        "description", "Episodio actualizado"),
                "startDate", request.getOrDefault(
                        "startDate", "2026-08-27"),
                "endDate", request.getOrDefault(
                        "endDate", "2026-08-27"),
                "isStatus", request.getOrDefault("isStatus", true)
        );

        return ResponseEntity.ok(response);
    }
}
