package com.uco.ucopetapi.controllers.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.VerificationStatus;
import com.uco.ucopetapi.domain.specialtieDoctor.mapper.SpecialtieDoctorMapper;
import com.uco.ucopetapi.dto.specialtieDoctor.SpecialtieDoctorDTO;
import com.uco.ucopetapi.dto.specialtieDoctor.SpecialtieDoctorRejectDTO;
import com.uco.ucopetapi.service.specialtieDoctor.SpecialtieDoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/specialtiedoctor")
public class SpecialtieDoctorController {

    private final SpecialtieDoctorService specialtieDoctorService;
    private final SpecialtieDoctorMapper specialtieDoctorMapper;

    public SpecialtieDoctorController(SpecialtieDoctorService specialtieDoctorService,
                                      SpecialtieDoctorMapper specialtieDoctorMapper) {
        this.specialtieDoctorService = specialtieDoctorService;
        this.specialtieDoctorMapper = specialtieDoctorMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findAllSpecialtieDoctor(@RequestParam UUID headquarterId) {
        List<SpecialtieDoctorDTO> specialtieDoctor = specialtieDoctorService.findAll(headquarterId)
                .stream().map(specialtieDoctorMapper::toDTO).toList();
        return ResponseEntity.ok(specialtieDoctor);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<SpecialtieDoctorDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(specialtieDoctorMapper.toDTO(specialtieDoctorService.findById(id)));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findByIdFilter(
            @RequestParam UUID headquarterId,
            @RequestParam(required = false) UUID idDoctor,
            @RequestParam(required = false) UUID idSpecialtie,
            @RequestParam(required = false) VerificationStatus status) {
        List<SpecialtieDoctorDTO> specialtieDoctor = specialtieDoctorService
                .findByFilter(headquarterId, idDoctor, idSpecialtie, status)
                .stream().map(specialtieDoctorMapper::toDTO).toList();
        return ResponseEntity.ok(specialtieDoctor);
    }

    @GetMapping("/verified")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findVerified(
            @RequestParam UUID headquarterId,
            @RequestParam UUID idSpecialtie) {
        List<SpecialtieDoctorDTO> specialtieDoctor = specialtieDoctorService
                .findVerified(headquarterId, idSpecialtie)
                .stream().map(specialtieDoctorMapper::toDTO).toList();
        return ResponseEntity.ok(specialtieDoctor);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<SpecialtieDoctorDTO> createNewSpecialtieDoctor(@RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        var created = specialtieDoctorService.createNewSpecialtieDoctor(specialtieDoctorMapper.toDomain(specialtieDoctor));
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtieDoctorMapper.toDTO(created));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<SpecialtieDoctorDTO> updateSpecialtieDoctor(
            @RequestParam(required = true) UUID id,
            @RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        var updated = specialtieDoctorService.updateSpecialtieDoctor(id, specialtieDoctorMapper.toDomain(specialtieDoctor));
        return ResponseEntity.ok(specialtieDoctorMapper.toDTO(updated));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Void> deactivateSpecialtieDoctor(@PathVariable UUID id) {
        specialtieDoctorService.deactivateSpecialtieDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialtieDoctorDTO> verifySpecialtieDoctor(@PathVariable UUID id) {
        var verified = specialtieDoctorService.verifySpecialtieDoctor(id);
        return ResponseEntity.ok(specialtieDoctorMapper.toDTO(verified));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialtieDoctorDTO> rejectSpecialtieDoctor(
            @PathVariable UUID id,
            @RequestBody SpecialtieDoctorRejectDTO request) {
        var rejected = specialtieDoctorService.rejectSpecialtieDoctor(id, request == null ? null : request.getReason());
        return ResponseEntity.ok(specialtieDoctorMapper.toDTO(rejected));
    }
}
