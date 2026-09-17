package com.uco.ucopetapi.controllers.healthplancoverage;

import com.uco.ucopetapi.dto.healthplancoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.service.healthplancoverage.HealthPlanCoverageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthplans/{healthPlanId}/coverages")
public class HealthPlanCoverageController {

    private final HealthPlanCoverageService coverageService;

    public HealthPlanCoverageController(
            HealthPlanCoverageService coverageService
    ) {
        this.coverageService = coverageService;
    }

    @GetMapping
    public ResponseEntity<List<HealthPlanCoverageDTO>> findAll(
            @PathVariable UUID healthPlanId) {

            return ResponseEntity.ok(
                    coverageService.findByHealthPlanId(
                            healthPlanId
                    )
            );
        }

    @GetMapping("/{coverageId}")
    public ResponseEntity<HealthPlanCoverageDTO> findById(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId
    ) {

        return ResponseEntity.ok(
                coverageService.findById(healthPlanId, coverageId)
        );
    }



    @PostMapping
    public ResponseEntity<HealthPlanCoverageDTO> save(
            @PathVariable UUID healthPlanId,
            @RequestBody HealthPlanCoverageDTO dto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        coverageService.save(
                                healthPlanId,
                                dto
                        )
                );
    }

    @PutMapping("/{coverageId}")
    public ResponseEntity<HealthPlanCoverageDTO> update(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId,
            @RequestBody HealthPlanCoverageDTO dto
    ) {

        return ResponseEntity.ok(
                coverageService.update(
                        healthPlanId,
                        coverageId,
                        dto
                )
        );
    }


    @PatchMapping("/{coverageId}")
    public ResponseEntity<HealthPlanCoverageDTO> patch(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId,
            @RequestBody HealthPlanCoverageDTO dto
    ) {

        return ResponseEntity.ok(
                coverageService.patch(
                        healthPlanId,
                        coverageId,
                        dto
                )
        );
    }

    @DeleteMapping("/{coverageId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId
    ) {

        coverageService.delete(
                healthPlanId,
                coverageId
        );

        return ResponseEntity.noContent().build();
    }
}