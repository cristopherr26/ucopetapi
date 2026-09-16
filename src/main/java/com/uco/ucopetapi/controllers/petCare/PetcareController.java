package com.uco.ucopetapi.controllers.petCare;

import com.uco.ucopetapi.dto.petCare.PetCareDto;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;
import com.uco.ucopetapi.service.petCare.PetCareService;
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
@RequestMapping("/api/v1/petcares")
public class PetcareController {

    private final PetCareService petCareService;

    public PetcareController(PetCareService petCareService) {
        this.petCareService = petCareService;
    }

    @GetMapping
    public ResponseEntity<List<PetCareDto>> findAll() {
        return ResponseEntity.ok(petCareService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetCareDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(petCareService.findById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<PetCareDto>> findByFilter(
            @RequestParam(required = false) UUID episodeId,
            @RequestParam(required = false) PetCareStatus petCareStatus,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(petCareService.findByFilter(episodeId, petCareStatus, description));
    }

    @PostMapping
    public ResponseEntity<PetCareDto> createPetCare(@RequestBody PetCareDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petCareService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetCareDto> updatePetCareById(@PathVariable UUID id, @RequestBody PetCareDto request) {
        return ResponseEntity.ok(petCareService.update(id, request));
    }
}
