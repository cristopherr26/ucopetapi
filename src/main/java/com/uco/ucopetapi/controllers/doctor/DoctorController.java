package com.uco.ucopetapi.controllers.doctor;

import com.uco.ucopetapi.domain.doctor.mapper.DoctorMapper;
import com.uco.ucopetapi.dto.doctor.DoctorDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.doctor.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/doctor")
public class DoctorController {

    private static final String MESSAGE_KEY = "message";

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    public DoctorController(DoctorService doctorService, DoctorMapper doctorMapper) {
        this.doctorService = doctorService;
        this.doctorMapper = doctorMapper;
    }

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> findAllDoctor() {
        List<DoctorDTO> doctors = doctorService.findAll().stream()
                .map(doctorMapper::toDTO)
                .toList();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findDoctorById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(doctorMapper.toDTO(doctorService.findById(id)));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<DoctorDTO>> findDoctorByFilter(@RequestParam(required = false) String licenseNumber) {
        List<DoctorDTO> doctors = doctorService.findByFilter(licenseNumber).stream()
                .map(doctorMapper::toDTO)
                .toList();
        return ResponseEntity.ok(doctors);
    }

    @PostMapping
    public ResponseEntity<Object> createNewDoctor(@RequestBody DoctorDTO doctor) {
        try {
            var createdDoctor = doctorService.createNewDoctor(doctorMapper.toDomain(doctor));
            return ResponseEntity.status(HttpStatus.CREATED).body(doctorMapper.toDTO(createdDoctor));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateDoctor(
            @RequestParam(required = true) UUID id,
            @RequestBody DoctorDTO doctor) {
        try {
            var updatedDoctor = doctorService.updateDoctor(id, doctorMapper.toDomain(doctor));
            return ResponseEntity.ok(doctorMapper.toDTO(updatedDoctor));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Object> deactivateDoctor(@PathVariable UUID id) {
        try {
            doctorService.deactivateDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Object> activateDoctor(@PathVariable UUID id) {
        try {
            doctorService.activateDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }
}