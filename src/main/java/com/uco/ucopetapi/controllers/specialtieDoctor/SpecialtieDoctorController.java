package com.uco.ucopetapi.controllers.specialtieDoctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.dto.specialtieDoctor.SpecialtieDoctorDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.doctor.DoctorService;
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
    private final DoctorService doctorService;

    public SpecialtieDoctorController(SpecialtieDoctorService specialtieDoctorService, DoctorService doctorService) {
        this.specialtieDoctorService = specialtieDoctorService;
        this.doctorService = doctorService;
    }

    private SpecialtieDoctorDTO toDTO(SpecialtieDoctorDomain specialtieDoctor) {
        UUID doctorId = specialtieDoctor.getDoctor() != null ? specialtieDoctor.getDoctor().getId() : null;
        return new SpecialtieDoctorDTO(specialtieDoctor.getId(), doctorId, specialtieDoctor.getIdSpecialtie());
    }

    // Solo busca el doctor si viene idDoctor en el body; si no viene, se deja
    // null y es validateFields(...) el que lanza el BusinessException (400),
    // en vez de que doctorService.findById(null) tire un error sin controlar.
    private SpecialtieDoctorDomain toDomain(SpecialtieDoctorDTO dto) {
        DoctorDomain doctor = dto.getDoctor() != null ? doctorService.findById(dto.getDoctor()) : null;
        return new SpecialtieDoctorDomain(dto.getId(), doctor, dto.getSpecialtie());
    }

    @GetMapping
    public ResponseEntity<List<SpecialtieDoctorDTO>> findAllSpecialtieDoctor() {
        List<SpecialtieDoctorDTO> specialtieDoctor = specialtieDoctorService.findAll().stream().map(this::toDTO).toList();
        return ResponseEntity.ok(specialtieDoctor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(toDTO(specialtieDoctorService.findById(id)));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findByIdFilter(
            @RequestParam(required = false) UUID idDoctor,
            @RequestParam(required = false) UUID idSpecialtie) {
        List<SpecialtieDoctorDTO> specialtieDoctor = specialtieDoctorService.findByFilter(idDoctor, idSpecialtie)
                .stream().map(this::toDTO).toList();
        return ResponseEntity.ok(specialtieDoctor);
    }

    @PostMapping
    public ResponseEntity<Object> createNewSpecialtieDoctor(@RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        try {
            SpecialtieDoctorDomain created = specialtieDoctorService.createNewSpecialtieDoctor(toDomain(specialtieDoctor));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(created));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateSpecialtieDoctor(
            @RequestParam(required = true) UUID id,
            @RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        try {
            SpecialtieDoctorDomain updated = specialtieDoctorService.updateSpecialtieDoctor(id, toDomain(specialtieDoctor));
            return ResponseEntity.ok(toDTO(updated));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Object> deactivateSpecialtieDoctor(@RequestParam(required = true) UUID id) {
        try {
            specialtieDoctorService.deactivateSpecialtieDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }
}