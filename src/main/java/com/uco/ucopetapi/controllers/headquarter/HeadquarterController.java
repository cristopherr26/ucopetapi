package com.uco.ucopetapi.controllers.headquarter;

import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import com.uco.ucopetapi.service.headquarter.HeadquarterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/headquarter")
public class HeadquarterController {

    private final HeadquarterService headquarterService;

    public HeadquarterController(HeadquarterService headquarterService) {
        this.headquarterService = headquarterService;
    }

    @GetMapping
    public ResponseEntity<List<HeadquarterDTO>> getAllHeadquarters() {
        return ResponseEntity.ok(headquarterService.getAllHeadquarters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeadquarterDTO> findHeadquarterById(@PathVariable UUID id) {
        return ResponseEntity.ok(headquarterService.findById(id));
    }

    @PostMapping
    public ResponseEntity<HeadquarterDTO> createNewHeadquarter(@Valid @RequestBody HeadquarterDTO headquarterDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(headquarterService.createNewHeadquarter(headquarterDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeadquarterDTO> updateHeadquarter(
            @PathVariable UUID id,
            @Valid @RequestBody HeadquarterDTO headquarterDTO) {
        return ResponseEntity.ok(headquarterService.updateHeadquarter(id, headquarterDTO));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<HeadquarterDTO> deactivateHeadquarter(@PathVariable UUID id) {
        return ResponseEntity.ok(headquarterService.deactivateHeadquarter(id));
    }
}