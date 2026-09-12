package com.uco.ucopetapi.controllers.pet;

import com.uco.ucopetapi.dto.pets.PetDTO;
import com.uco.ucopetapi.service.pet.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping
    public ResponseEntity<List<PetDTO>> getAllPets(
            @RequestParam UUID headquarterId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(petService.getAllPets(headquarterId, name, breed, species, isActive));
    }

    @PostMapping
    public ResponseEntity<PetDTO> createPet(@RequestBody PetDTO request) {
        PetDTO response = petService.createPet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetDTO> updatePet(
            @PathVariable UUID id,
            @RequestBody PetDTO request) {
        return ResponseEntity.ok(petService.updatePet(id, request));
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<PetDTO>> getPetsByTutor(
            @PathVariable UUID tutorId,
            @RequestParam UUID headquarterId) {
        return ResponseEntity.ok(petService.getPetsByTutor(tutorId, headquarterId));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PetDTO> deactivatePet(@PathVariable UUID id) {
        return ResponseEntity.ok(petService.deactivatePet(id));
    }
}