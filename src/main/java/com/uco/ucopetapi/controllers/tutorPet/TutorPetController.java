package com.uco.ucopetapi.controllers.tutorPet;

import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.service.tutorPet.TutorPetService;
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

    private final TutorPetService tutorPetService;

    public TutorPetController(TutorPetService tutorPetService) {
        this.tutorPetService = tutorPetService;
    }

    @GetMapping
    public ResponseEntity<List<TutorPetDTO>> findAllTutorPet() {
        return ResponseEntity.ok(tutorPetService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorPetDTO> findTutorPetById(@PathVariable UUID id) {
        return ResponseEntity.ok(tutorPetService.findById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TutorPetDTO>> findTutorPetByFilter(@RequestParam(required = true) UUID id) {
        return ResponseEntity.ok(tutorPetService.findByPersonId(id));
    }

    @PostMapping
    public ResponseEntity<TutorPetDTO> createNewTutorPet(@RequestBody TutorPetDTO tutorPet) {
        TutorPetDTO createdTutorPet = tutorPetService.create(tutorPet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTutorPet);
    }

    @PutMapping
    public ResponseEntity<TutorPetDTO> updateTutorPet(
            @RequestParam(required = true) UUID id,
            @RequestBody TutorPetDTO tutorPet) {

        TutorPetDTO updatedTutorPet = tutorPetService.update(id, tutorPet);
        return ResponseEntity.ok(updatedTutorPet);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Void> deactivateTutorPet(@RequestParam(required = true) UUID id) {
        tutorPetService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

}
