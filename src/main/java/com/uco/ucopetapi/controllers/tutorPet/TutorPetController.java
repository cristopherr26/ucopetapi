package com.uco.ucopetapi.controllers.tutorPet;

import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
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
@RequestMapping("api/v1/tutorPet")
public class TutorPetController {

    private TutorPetDTO buildDummyTutorPet(UUID id) {
        return new TutorPetDTO(
                id != null ? id : UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    @GetMapping
    public ResponseEntity<List<TutorPetDTO>> findAllTutorPet() {
        List<TutorPetDTO> tutorPet = List.of(
                buildDummyTutorPet(UUID.randomUUID())
        );

        return ResponseEntity.ok(tutorPet);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorPetDTO> findTutorPetById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildDummyTutorPet(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TutorPetDTO>> findTutorPetByFilter(@RequestParam(required = true) UUID id) {
        return ResponseEntity.ok(List.of(buildDummyTutorPet(id)));
    }

    @PostMapping
    public ResponseEntity<TutorPetDTO> createNewTutorPet(@RequestBody TutorPetDTO tutorPet) {
        TutorPetDTO createdTutorPet = buildDummyTutorPet(tutorPet.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTutorPet);
    }

    @PutMapping
    public ResponseEntity<TutorPetDTO> updateTutorPet(
            @RequestParam(required = true) UUID id,
            @RequestBody TutorPetDTO tutorPet) {

        TutorPetDTO updatedTutorPet = buildDummyTutorPet(id);
        return ResponseEntity.ok(updatedTutorPet);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Void> deactivateTutorPet(@RequestParam(required = true) UUID id) {
        return ResponseEntity.noContent().build();
    }

}
