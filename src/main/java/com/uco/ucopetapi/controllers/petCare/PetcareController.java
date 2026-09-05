package com.uco.ucopetapi.controllers.petCare;


import com.uco.ucopetapi.dto.petCare.PetCareDto;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;
import com.uco.ucopetapi.dto.vitalSigns.VitalSignsDTO;
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
@RequestMapping("/api/v1/petcares")
public class PetcareController {

    private VitalSignsDTO buildVitalSigns() {
        return new VitalSignsDTO(
                38.5,
                90,
                24,
                120,
                80,
                12.4,
                5,
                LocalDateTime.of(2026, 8, 27, 10, 30)
        );
    }

    private PetCareDto buildPetCare(final UUID id, final UUID episodeId, final String description,
                                    final PetCareStatus status) {
        return new PetCareDto(
                id,
                episodeId,
                UUID.fromString("b1d8c2f4-7d3a-4c4c-9f3c-3fb7f0b9a002"),
                UUID.fromString("e5c0a3b4-6d7e-4f8a-9b0c-1d2e3f4a5b6c"),
                UUID.fromString("f6d1b4c5-7e8f-4a9b-8c0d-2e3f4a5b6c7d"),
                LocalDateTime.of(2026, 8, 27, 10, 0),
                description,
                status,
                buildVitalSigns()
        );
    }

    @GetMapping
    public ResponseEntity<List<PetCareDto>> findAll() {
        List<PetCareDto> petCares = List.of(
                buildPetCare(
                        UUID.fromString("aa11bb22-cc33-4d44-8e55-ff6677889900"),
                        UUID.fromString("c3a8d1e2-4b5f-4a6c-9d0e-1f2a3b4c5d6e"),
                        "Consulta y atención veterinaria",
                        PetCareStatus.COMPLETED
                )
        );
        return ResponseEntity.ok(petCares);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetCareDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildPetCare(
                id,
                UUID.fromString("c3a8d1e2-4b5f-4a6c-9d0e-1f2a3b4c5d6e"),
                "Atención veterinaria",
                PetCareStatus.IN_PROGRESS
        ));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<PetCareDto>> findByFilter(
            @RequestParam(required = false) UUID episodeId,
            @RequestParam(required = false) PetCareStatus petCateStatus,
            @RequestParam(required = false) String description) {

        UUID resolvedEpisodeId = episodeId != null
                ? episodeId
                : UUID.fromString("c3a8d1e2-4b5f-4a6c-9d0e-1f2a3b4c5d6e");
        PetCareStatus status = petCateStatus != null ? petCateStatus : PetCareStatus.REGISTERED;
        String filterDescription = description != null ? description : "Resultado filtrado";

        return ResponseEntity.ok(List.of(
                buildPetCare(UUID.randomUUID(), resolvedEpisodeId, filterDescription, status)
        ));
    }

    @PostMapping
    public ResponseEntity<PetCareDto> createPetCare(@RequestBody PetCareDto request) {
        UUID id = request.getId() != null ? request.getId() : UUID.randomUUID();
        PetCareDto created = new PetCareDto(
                id,
                request.getEpisodeId(),
                request.getProcedureId(),
                request.getProductId(),
                request.getDoctorId(),
                request.getAttentionDate() != null ? request.getAttentionDate() : LocalDateTime.now(),
                request.getDescription() != null ? request.getDescription() : "Atención sin descripción",
                request.petCateStatus() != null ? request.petCateStatus() : PetCareStatus.REGISTERED,
                request.getVitalSigns() != null ? request.getVitalSigns() : buildVitalSigns()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetCareDto> updatePetCareById(
            @PathVariable UUID id,
            @RequestBody PetCareDto request) {

        PetCareDto updated = new PetCareDto(
                id,
                request.getEpisodeId(),
                request.getProcedureId(),
                request.getProductId(),
                request.getDoctorId(),
                request.getAttentionDate() != null ? request.getAttentionDate() : LocalDateTime.now(),
                request.getDescription() != null ? request.getDescription() : "Atención actualizada",
                request.petCateStatus() != null ? request.petCateStatus() : PetCareStatus.IN_PROGRESS,
                request.getVitalSigns() != null ? request.getVitalSigns() : buildVitalSigns()
        );
        return ResponseEntity.ok(updated);
    }
}
