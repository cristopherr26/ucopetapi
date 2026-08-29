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

    @GetMapping("/{id}")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildDummySpecialtieDoctor(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findByIdFilter(@RequestParam (required = false) UUID id){
        return ResponseEntity.ok(List.of(buildDummySpecialtieDoctor(id)));
    }

    