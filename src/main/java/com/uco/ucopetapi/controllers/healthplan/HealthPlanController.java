package com.uco.ucopetapi.controllers.healthplan;

import com.uco.ucopetapi.dto.healthplan.HealthPlanDTO;
import com.uco.ucopetapi.service.healthplan.HealthPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthplans")

public class HealthPlanController {
    private final HealthPlanService healthPlanService;

    public HealthPlanController(
            HealthPlanService healthPlanService
    ) {
        this.healthPlanService = healthPlanService;
    }


    @GetMapping
    public ResponseEntity<List<HealthPlanDTO>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String insuranceCompany
    ) {

            if (name != null) {
                return ResponseEntity.ok(
                        healthPlanService.findByName(name)
                );
            }

            if (insuranceCompany != null) {
                return ResponseEntity.ok(
                        healthPlanService.findByInsuranceCompany(
                                insuranceCompany
                        )
                );
            }

            return ResponseEntity.ok(
                    healthPlanService.findAll()
            );
        }


    @GetMapping("/{id}")
    public ResponseEntity<HealthPlanDTO> findById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                healthPlanService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<HealthPlanDTO> save(
            @RequestBody HealthPlanDTO dto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        healthPlanService.save(dto)
                );
    }



    @PutMapping("/{id}")
    public ResponseEntity<HealthPlanDTO> update(
            @PathVariable UUID id,
            @RequestBody HealthPlanDTO dto
    ) {

        return ResponseEntity.ok(
                healthPlanService.update(id, dto)
        );
    }


    @PatchMapping("/{id}")
    public ResponseEntity<HealthPlanDTO> patch(
            @PathVariable UUID id,
            @RequestBody HealthPlanDTO dto
    ) {

        return ResponseEntity.ok(
                healthPlanService.patch(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {

        healthPlanService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
