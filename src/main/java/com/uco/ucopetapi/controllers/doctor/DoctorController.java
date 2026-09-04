package com.uco.ucopetapi.controllers.doctor;

import com.uco.ucopetapi.dto.doctor.DoctorDTO;
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
@RequestMapping("api/v1/doctor")
public class DoctorController {

    private DoctorDTO buildDummyDoctor(UUID id) {
        return new DoctorDTO(
                id != null ? id : UUID.randomUUID(),
                UUID.randomUUID(),
                "MED-2026-001"
        );
    }

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> findAllDoctor() {
        List<DoctorDTO> doctor = List.of(
                buildDummyDoctor(UUID.randomUUID())
        );

        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> findDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildDummyDoctor(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<DoctorDTO>> findDoctorByFilter(@RequestParam(required = true) UUID id) {
        return ResponseEntity.ok(List.of(buildDummyDoctor(id)));
    }

    @PostMapping
    public ResponseEntity<DoctorDTO> createNewDoctor(@RequestBody DoctorDTO doctor) {
        DoctorDTO createdDoctor = buildDummyDoctor(doctor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDoctor);
    }

    @PutMapping
    public ResponseEntity<DoctorDTO> updateDoctor(
            @RequestParam(required = true) UUID id,
            @RequestBody DoctorDTO doctor) {

        DoctorDTO updatedDoctor = buildDummyDoctor(id);
        return ResponseEntity.ok(updatedDoctor);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Void> deactivateDoctor(@RequestParam(required = true) UUID id) {
        return ResponseEntity.noContent().build();
    }

}