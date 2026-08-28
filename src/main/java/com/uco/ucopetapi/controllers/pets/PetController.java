package com.uco.ucopetapi.controllers.pets;

import com.uco.ucopetapi.dto.pets.PetDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pets")
public class PetController {

    @GetMapping
    public ResponseEntity<List<PetDTO>> getAllPets() {
        List<PetDTO> pets = List.of(
                new PetDTO(
                        UUID.fromString("a1e6f0d2-8d1e-4f3b-9a2c-1b2c3d4e5f6a"),
                        "Toby", LocalDate.of(2021, 3, 10), "Labrador", "Perro", "Macho",
                        "https://blob.ucopet.com/pets/toby.jpg",
                        UUID.fromString("3f2a1c9e-9e2f-5a4c-8b3d-2c3d4e5f6a7b"),
                        UUID.fromString("9b7d4e21-1a2b-4c3d-8e9f-6a7b8c9d0e1f"),
                        true
                ),
                new PetDTO(
                        UUID.fromString("b2f7e1d3-9e2f-5a4c-8b3d-2c3d4e5f6a7b"),
                        "Mila", LocalDate.of(2022, 7, 1), "Poodle", "Perro", "Hembra",
                        "https://blob.ucopet.com/pets/mila.jpg",
                        UUID.fromString("4a3b2c1d-8d1e-4f3b-9a2c-1b2c3d4e5f6a"),
                        UUID.fromString("8c6e3d10-9a2c-1b2c-3d4e-5f6a7b8c9d0e"),
                        true
                )
        );
        return ResponseEntity.ok(pets);
    }

    @PostMapping
    public ResponseEntity<PetDTO> createPet(@RequestBody PetDTO request) {
        PetDTO response = new PetDTO(
                UUID.randomUUID(),
                request.getName(),
                request.getBirthDate(),
                request.getBreed(),
                request.getSpecies(),
                request.getGender(),
                request.getPhotoUrl(),
                request.getTutorId(),
                request.getPolicyId(),
                true
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetDTO> updatePet(
            @PathVariable UUID id,
            @RequestBody PetDTO request) {

        PetDTO response = new PetDTO(
                id,
                request.getName(),
                request.getBirthDate(),
                request.getBreed(),
                request.getSpecies(),
                request.getGender(),
                request.getPhotoUrl(),
                request.getTutorId(),
                request.getPolicyId(),
                true
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<PetDTO>> getPetsByTutor(@PathVariable UUID tutorId) {
        List<PetDTO> pets = List.of(
                new PetDTO(
                        UUID.fromString("a1e6f0d2-8d1e-4f3b-9a2c-1b2c3d4e5f6a"),
                        "Toby", LocalDate.of(2021, 3, 10), "Labrador", "Perro", "Macho",
                        "https://blob.ucopet.com/pets/toby.jpg",
                        tutorId,
                        UUID.fromString("9b7d4e21-1a2b-4c3d-8e9f-6a7b8c9d0e1f"),
                        true
                )
        );
        return ResponseEntity.ok(pets);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PetDTO> deactivatePet(@PathVariable UUID id) {
        PetDTO response = new PetDTO();
        response.setId(id);
        response.setActive(false);
        return ResponseEntity.ok(response);
    }
}
