package com.uco.ucopetapi.controllers.specialtieDoctor;

import com.uco.ucopetapi.dto.specialtieDoctor.SpecialtieDoctorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/specialtiedoctor")
public class SpecialtieDoctorController {

    private SpecialtieDoctorDTO buildDummySpecialtieDoctor(UUID id) {
        return new SpecialtieDoctorDTO(
                id != null ? id : UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }

    @GetMapping
    public ResponseEntity<List<SpecialtieDoctorDTO>> findAllSpecialtieDoctor() {
        List<SpecialtieDoctorDTO> specialtieDoctor = List.of(
                buildDummySpecialtieDoctor(UUID.randomUUID())
        );

        return ResponseEntity.ok(specialtieDoctor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialtieDoctorDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildDummySpecialtieDoctor(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findByIdFilter(@RequestParam(required = false) UUID id) {
        return ResponseEntity.ok(List.of(buildDummySpecialtieDoctor(id)));
    }

    @PostMapping
    public ResponseEntity<SpecialtieDoctorDTO> createNewSpecialtieDoctor(@RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        SpecialtieDoctorDTO createdSpecialtieDoctor = buildDummySpecialtieDoctor(specialtieDoctor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSpecialtieDoctor);
    }

    @PutMapping
    public ResponseEntity<SpecialtieDoctorDTO> updateSpecialtieDoctor(
            @RequestParam(required = true) UUID id,
            @RequestBody SpecialtieDoctorDTO specialtieDoctor) {

        SpecialtieDoctorDTO updatedSpecialtieDoctor = buildDummySpecialtieDoctor(id);
        return ResponseEntity.ok(updatedSpecialtieDoctor);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Void> deactivateSpecialtieDoctor(@RequestParam(required = true) UUID id) {
        return ResponseEntity.noContent().build();
    }

}