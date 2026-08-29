package com.uco.ucopetapi.controllers.petCare;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/petcare")
public class PetcareController {

    // FIND ALL
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> findAll() {

        List<Map<String, Object>> petCares = List.of(
                Map.of(
                        "id", UUID.randomUUID().toString(),
                        "petsDto", UUID.randomUUID().toString(),
                        "episodeDTO", UUID.randomUUID().toString(),
                        "procedureDTO", UUID.randomUUID().toString(),
                        "productDTO", UUID.randomUUID().toString(),
                        "attentionDate", "2026-08-27",
                        "description", "Consulta y atención veterinaria",
                        "isPetCareStatus", true
                )
        );

        return ResponseEntity.ok(petCares);
    }

    // FIND BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(
            @PathVariable String id) {

        Map<String, Object> petCare = Map.of(
                "id", id,
                "petsDto", UUID.randomUUID().toString(),
                "episodeDTO", UUID.randomUUID().toString(),
                "procedureDTO", UUID.randomUUID().toString(),
                "productDTO", UUID.randomUUID().toString(),
                "attentionDate", "2026-08-27",
                "description", "Atención veterinaria",
                "isPetCareStatus", true
        );

        return ResponseEntity.ok(petCare);
    }

    // FIND BY FILTER
    @GetMapping("/filter")
    public ResponseEntity<List<Map<String, Object>>> findByFilter(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPetCareStatus) {

        List<Map<String, Object>> petCares = List.of(
                Map.of(
                        "id", UUID.randomUUID().toString(),
                        "description", description != null
                                ? description
                                : "Resultado filtrado",
                        "isPetCareStatus",
                        isPetCareStatus != null ? isPetCareStatus : true
                )
        );

        return ResponseEntity.ok(petCares);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPetCare(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", UUID.randomUUID().toString(),
                "petsDto", request.getOrDefault("petsDto", ""),
                "episodeDTO", request.getOrDefault("episodeDTO", ""),
                "procedureDTO", request.getOrDefault("procedureDTO", ""),
                "productDTO", request.getOrDefault("productDTO", ""),
                "attentionDate", request.getOrDefault(
                        "attentionDate", "2026-08-27"),
                "description", request.getOrDefault(
                        "description", "Atención sin descripción"),
                "isPetCareStatus", request.getOrDefault(
                        "isPetCareStatus", true)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePetCareById(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", id,
                "description", request.getOrDefault(
                        "description", "Atención actualizada"),
                "isPetCareStatus", request.getOrDefault(
                        "isPetCareStatus", true)
        );

        return ResponseEntity.ok(response);
    }

}
