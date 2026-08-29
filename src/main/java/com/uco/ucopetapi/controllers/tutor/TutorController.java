package com.uco.ucopetapi.controllers.tutor;

import com.uco.ucopetapi.dto.tutor.TutorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tutors")
public class TutorController {


    private static final TutorDTO SAMPLE_TUTOR = new TutorDTO(
            UUID.fromString("3f1a9c2e-7b4d-4e9a-a123-9de8b1a2c3d4"),
            "1000442701",
            "Michel",
            "Guarnizo",
            "micheldelosrios28@gmail.com",
            "322621355",
            "Cra 27 #40-82, El Carmen",
            true,
            null
    );

    private static final TutorDTO SAMPLE_TUTOR_2 = new TutorDTO(
            UUID.fromString("7a2b1c3d-4e5f-6789-abcd-ef1234567890"),
            "93384112",
            "Matias Alejandro",
            "Mora",
            "matias.mora@hotmail.com",
            "3128842201",
            "Calle 12 #5-40, Medellin",
            true,
            null
    );

    @PostMapping
    public ResponseEntity<TutorDTO> createNewTutor(@RequestBody TutorDTO tutor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SAMPLE_TUTOR);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TutorDTO> updateTutor(@PathVariable UUID id, @RequestBody TutorDTO tutor) {
        return ResponseEntity.ok(SAMPLE_TUTOR);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateTutor(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(SAMPLE_TUTOR);
    }

    @GetMapping(params = {"nombre"})
    public ResponseEntity<List<TutorDTO>> findByFilter(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(List.of(SAMPLE_TUTOR));
    }

    @GetMapping
    public ResponseEntity<List<TutorDTO>> findAll() {
        return ResponseEntity.ok(List.of(SAMPLE_TUTOR, SAMPLE_TUTOR_2));
    }
}
