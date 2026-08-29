package com.uco.ucopetapi.controllers.healthplan;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthplans")

public class HealthPlanController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllHealthPlans(
            @RequestParam(required = false) String insuranceCompany) {

        List<Map<String, Object>> healthPlans = List.of(
                Map.of(
                        "id", UUID.fromString("105bd9b5-9db9-47df-bd1e-b3fb8271420b"),
                        "name", "Plan Mascotas Premium",
                        "insuranceCompany", "SURA",
                        "description", "Plan de cobertura integral para mascotas",
                        "deleted", false
                ),
                Map.of(
                        "id", UUID.fromString("205bd9b5-9db9-47df-bd1e-b3fb8271420b"),
                        "name", "Plan Mascotas Básico",
                        "insuranceCompany", "MAPFRE",
                        "description", "Plan de cobertura básica para mascotas",
                        "deleted", false
                )
        );

        if (insuranceCompany != null) {
            healthPlans = healthPlans.stream()
                    .filter(plan ->
                            plan.get("insuranceCompany")
                                    .toString()
                                    .equalsIgnoreCase(insuranceCompany)
                    )
                    .toList();
        }

        return ResponseEntity.ok(healthPlans);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getHealthPlanById(
            @PathVariable UUID id) {

        Map<String, Object> healthPlan = Map.of(
                "id", id,
                "name", "Plan Mascotas Premium",
                "insuranceCompany", "SURA",
                "description", "Plan de cobertura integral para mascotas",
                "deleted", false
        );

        return ResponseEntity.ok(healthPlan);
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> createHealthPlan(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", UUID.randomUUID(),
                "name", request.getOrDefault(
                        "name",
                        "Plan Mascotas Premium"
                ),
                "insuranceCompany", request.getOrDefault(
                        "insuranceCompany",
                        "SURA"
                ),
                "description", request.getOrDefault(
                        "description",
                        "Plan de cobertura integral para mascotas"
                ),
                "deleted", false
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHealthPlan(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", id,
                "name", request.getOrDefault(
                        "name",
                        "Plan Mascotas Premium Actualizado"
                ),
                "insuranceCompany", "SURA",
                "description", request.getOrDefault(
                        "description",
                        "Descripción actualizada del plan"

                ),
                "deleted", false
        );

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> patchHealthPlan(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", id,
                "name", request.getOrDefault(
                        "name",
                        "Plan Mascotas Premium"
                ),
                "insuranceCompany", "SURA",
                "description", request.getOrDefault(
                        "description",
                        "Descripción actualizada"

                ),
                "deleted", false
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHealthPlan(
            @PathVariable UUID id) {

        Map<String, Object> response = Map.of(
                "id", id,
                "message", "Health plan deleted successfully",
                "deleted", true
        );

        return ResponseEntity.ok(response);
    }
}
