package com.uco.ucopetapi.controllers.healthplancoverage;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthplans/{healthPlanId}/coverages")
public class HealthPlanCoverageController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCoverages(
            @PathVariable UUID healthPlanId) {

        List<Map<String, Object>> coverages = List.of(
                Map.of(
                        "id", UUID.fromString(
                                "305bd9b5-9db9-47df-bd1e-b3fb8271420b"
                        ),
                        "healthPlanId", healthPlanId,
                        "procedureId", UUID.fromString(
                                "405bd9b5-9db9-47df-bd1e-b3fb8271420b"
                        ),
                        "coveragePercentage", 80,
                        "coverageLimit", new BigDecimal("500000")
                ),
                Map.of(
                        "id", UUID.fromString(
                                "505bd9b5-9db9-47df-bd1e-b3fb8271420b"
                        ),
                        "healthPlanId", healthPlanId,
                        "procedureId", UUID.fromString(
                                "605bd9b5-9db9-47df-bd1e-b3fb8271420b"
                        ),
                        "coveragePercentage", 90,
                        "coverageLimit", new BigDecimal("2000000")
                )
        );

        return ResponseEntity.ok(coverages);
    }


    @GetMapping("/{coverageId}")
    public ResponseEntity<Map<String, Object>> getCoverageById(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId) {

        Map<String, Object> coverage = Map.of(
                "id", coverageId,
                "healthPlanId", healthPlanId,
                "procedureId", UUID.fromString(
                        "405bd9b5-9db9-47df-bd1e-b3fb8271420b"
                ),
                "coveragePercentage", 80,
                "coverageLimit", new BigDecimal("500000")
        );

        return ResponseEntity.ok(coverage);
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> createCoverage(
            @PathVariable UUID healthPlanId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", UUID.randomUUID(),
                "healthPlanId", healthPlanId,
                "procedureId", request.getOrDefault(
                        "procedureId",
                        UUID.randomUUID()
                ),
                "coveragePercentage", request.getOrDefault(
                        "coveragePercentage",
                        80
                ),
                "coverageLimit", request.getOrDefault(
                        "coverageLimit",
                        new BigDecimal("500000")
                )
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PutMapping("/{coverageId}")
    public ResponseEntity<Map<String, Object>> updateCoverage(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", coverageId,
                "healthPlanId", healthPlanId,
                "procedureId", request.getOrDefault(
                        "serviceId",
                        UUID.randomUUID()
                ),
                "coveragePercentage", request.getOrDefault(
                        "coveragePercentage",
                        90
                ),
                "coverageLimit", request.getOrDefault(
                        "coverageLimit",
                        new BigDecimal("1000000")
                )
        );

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{coverageId}")
    public ResponseEntity<Map<String, Object>> patchCoverage(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", coverageId,
                "healthPlanId", healthPlanId,
                "procedureId", request.getOrDefault(
                        "serviceId",
                        UUID.randomUUID()
                ),
                "coveragePercentage", request.getOrDefault(
                        "coveragePercentage",
                        80
                ),
                "coverageLimit", request.getOrDefault(
                        "coverageLimit",
                        new BigDecimal("500000")
                )
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{coverageId}")
    public ResponseEntity<Map<String, Object>> deleteCoverage(
            @PathVariable UUID healthPlanId,
            @PathVariable UUID coverageId) {

        Map<String, Object> response = Map.of(
                "id", coverageId,
                "healthPlanId", healthPlanId,
                "message", "Coverage deleted successfully"
        );

        return ResponseEntity.ok(response);
    }
}