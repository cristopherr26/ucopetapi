package com.uco.ucopetapi.controllers.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.mapper.SpecialtieDoctorMapper;
import com.uco.ucopetapi.dto.specialtieDoctor.SpecialtieDoctorDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.specialtieDoctor.SpecialtieDoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/specialtiedoctor")
public class SpecialtieDoctorController {

    private static final String MESSAGE_KEY = "message";

    private final SpecialtieDoctorService specialtieDoctorService;
    private final SpecialtieDoctorMapper specialtieDoctorMapper;

    public SpecialtieDoctorController(SpecialtieDoctorService specialtieDoctorService,
                                      SpecialtieDoctorMapper specialtieDoctorMapper) {
        this.specialtieDoctorService = specialtieDoctorService;
        this.specialtieDoctorMapper = specialtieDoctorMapper;
    }

    @GetMapping
    public ResponseEntity<List<SpecialtieDoctorDTO>> findAllSpecialtieDoctor() {
        List<SpecialtieDoctorDTO> specialtieDoctor = specialtieDoctorService.findAll()
                .stream().map(specialtieDoctorMapper::toDTO).toList();
        return ResponseEntity.ok(specialtieDoctor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(specialtieDoctorMapper.toDTO(specialtieDoctorService.findById(id)));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findByIdFilter(
            @RequestParam(required = false) UUID idDoctor,
            @RequestParam(required = false) UUID idSpecialtie) {
        List<SpecialtieDoctorDTO> specialtieDoctor = specialtieDoctorService.findByFilter(idDoctor, idSpecialtie)
                .stream().map(specialtieDoctorMapper::toDTO).toList();
        return ResponseEntity.ok(specialtieDoctor);
    }

    @PostMapping
    public ResponseEntity<Object> createNewSpecialtieDoctor(@RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        try {
            var created = specialtieDoctorService.createNewSpecialtieDoctor(specialtieDoctorMapper.toDomain(specialtieDoctor));
            return ResponseEntity.status(HttpStatus.CREATED).body(specialtieDoctorMapper.toDTO(created));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateSpecialtieDoctor(
            @RequestParam(required = true) UUID id,
            @RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        try {
            var updated = specialtieDoctorService.updateSpecialtieDoctor(id, specialtieDoctorMapper.toDomain(specialtieDoctor));
            return ResponseEntity.ok(specialtieDoctorMapper.toDTO(updated));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Object> deactivateSpecialtieDoctor(@PathVariable UUID id) {
        try {
            specialtieDoctorService.deactivateSpecialtieDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }
}