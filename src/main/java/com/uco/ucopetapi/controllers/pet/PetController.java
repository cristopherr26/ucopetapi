package com.uco.ucopetapi.controllers.pet;

import com.uco.ucopetapi.dto.pet.PetDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.pet.PetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    private static final String MESSAGE = "message:";

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ResponseEntity<List<PetDTO>> getAllPets(
            @RequestParam UUID headquarterId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(
                petService.getAllPets(
                        headquarterId,
                        name,
                        breed,
                        species,
                        isActive
                )
        );
    }

    @PostMapping
    public ResponseEntity<Object> createPet(@RequestBody PetDTO request) {

        try {
            PetDTO response = petService.createPet(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (BusinessException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(MESSAGE, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePet(
            @PathVariable UUID id,
            @RequestBody PetDTO request) {

        try {
            PetDTO response = petService.updatePet(id, request);

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(MESSAGE, e.getMessage()));
        }
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<PetDTO>> getPetsByTutor(
            @PathVariable UUID tutorId,
            @RequestParam UUID headquarterId) {

        return ResponseEntity.ok(
                petService.getPetsByTutor(tutorId, headquarterId)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Object> deactivatePet(
            @PathVariable UUID id) {

        try {
            PetDTO response = petService.deactivatePet(id);

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(MESSAGE, e.getMessage()));
        }
    }
}