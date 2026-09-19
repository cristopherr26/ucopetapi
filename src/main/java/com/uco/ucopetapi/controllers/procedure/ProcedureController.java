package com.uco.ucopetapi.controllers.procedure;

import com.uco.ucopetapi.dto.space.SpaceDTO;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.service.procedure.ProcedureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/procedures")
public class ProcedureController {

    private final ProcedureService procedureService;

    public ProcedureController(final ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @GetMapping
    public ResponseEntity<List<ProcedureDTO>> findAllProcedures() {
        return ResponseEntity.ok(procedureService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProcedureDTO>> findActiveProcedures() {
        return ResponseEntity.ok(procedureService.findActiveProcedures());
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<ProcedureDTO>> findInactiveProcedures() {
        return ResponseEntity.ok(procedureService.findInactiveProcedures());
    }

    @GetMapping("/active-spaces")
    public ResponseEntity<List<SpaceDTO>> findActiveSpacesForProcedureCreation() {
        return ResponseEntity.ok(procedureService.findActiveSpacesForProcedureCreation());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ProcedureDTO> findProcedureByCode(@PathVariable final String code) {
        return ResponseEntity.ok(procedureService.findByCode(code));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcedureDTO> findProcedureById(@PathVariable final UUID id) {
        return ResponseEntity.ok(procedureService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProcedureDTO> createProcedure(@RequestBody final ProcedureDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(procedureService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcedureDTO> updateProcedure(
            @PathVariable final UUID id,
            @RequestBody final ProcedureDTO request) {
        return ResponseEntity.ok(procedureService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedureDTO> deactivateProcedure(@PathVariable final UUID id) {
        return ResponseEntity.ok(procedureService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedureDTO> activateProcedure(@PathVariable final UUID id) {
        return ResponseEntity.ok(procedureService.activate(id));
    }
}
