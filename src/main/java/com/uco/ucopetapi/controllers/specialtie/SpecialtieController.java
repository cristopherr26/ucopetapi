package com.uco.ucopetapi.controllers.specialtie;

import com.uco.ucopetapi.dto.specialtie.SpecialtieDTO;
import com.uco.ucopetapi.service.specialtie.SpecialtieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/specialties")
public class SpecialtieController {

    private final SpecialtieService specialtieService;

    public SpecialtieController(SpecialtieService specialtieService) {
        this.specialtieService = specialtieService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<SpecialtieDTO>> getAllSpecialties() {
        List<SpecialtieDTO> dtos = specialtieService.getAllSpecialties();
        return  ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<SpecialtieDTO> findSpecialtieById(@PathVariable UUID id) {

        SpecialtieDTO dto = specialtieService.findSpecialtieById(id);

        return ResponseEntity.ok(dto);
    }

    @GetMapping(params = "name")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<SpecialtieDTO> findSpecialtieByName (@RequestParam String name) {

        SpecialtieDTO dto = specialtieService.findSpecialtieByName(name);

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createSpecialtie(@RequestBody SpecialtieDTO dto) {
        specialtieService.createNewSpecialtie(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body("La especialidad fue creada exitosamente.");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> patchSpecialtie(@PathVariable UUID id, @RequestBody SpecialtieDTO dto) {
        specialtieService.patchSpecialtie(id, dto);

        return ResponseEntity.ok("La especialidad fue actualizada exitosamente.");
    }
}
