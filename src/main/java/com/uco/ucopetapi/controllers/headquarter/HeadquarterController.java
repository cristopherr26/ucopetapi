package com.uco.ucopetapi.controllers.headquarter;

import com.uco.ucopetapi.domain.specialtie.HeadquarterDomain;
import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import com.uco.ucopetapi.service.specialtie.HeadquarterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/headquarter")
public class HeadquarterController {

    private final HeadquarterService headquarterService;

    public HeadquarterController(HeadquarterService headquarterService) {
        this.headquarterService = headquarterService;
    }

    private HeadquarterDTO toDTO(HeadquarterDomain entity) {
        return new HeadquarterDTO(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getIsActive()
        );
    }

    private HeadquarterDomain toEntity(HeadquarterDTO dto) {
        return new HeadquarterDomain(
                dto.getId(),
                dto.getName(),
                dto.getAddress(),
                dto.getIsActive()
        );
    }

    @GetMapping
    public ResponseEntity<List<HeadquarterDTO>> findAll() {
        List<HeadquarterDTO> list = headquarterService.get().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeadquarterDTO> findById(@PathVariable UUID id) {
        return headquarterService.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HeadquarterDTO> createNewHeadquarter(@Valid @RequestBody HeadquarterDTO headquarterDTO) {
        HeadquarterDomain entity = toEntity(headquarterDTO);
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        HeadquarterDomain saved = headquarterService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeadquarterDTO> updateHeadquarter(
            @PathVariable UUID id,
            @Valid @RequestBody HeadquarterDTO headquarterDTO) {

        return headquarterService.findById(id).map(existing -> {
            existing.setName(headquarterDTO.getName());
            existing.setAddress(headquarterDTO.getAddress());
            if (headquarterDTO.getIsActive() != null) {
                existing.setIsActive(headquarterDTO.getIsActive());
            }
            HeadquarterDomain updated = headquarterService.save(existing);
            return ResponseEntity.ok(toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<HeadquarterDTO> deactivateHeadquarter(@PathVariable UUID id) {
        return headquarterService.deactivate(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}