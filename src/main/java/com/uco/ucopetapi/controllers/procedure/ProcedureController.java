package com.uco.ucopetapi.controllers.procedure;

import com.uco.ucopetapi.dto.space.SpaceDTO;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.service.procedure.IProcedureService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/procedures")
public class ProcedureController {

    private final IProcedureService procedureService;

    public ProcedureController(final IProcedureService procedureService) {
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
    public ResponseEntity<Map<String, String>> createProcedure(@RequestBody final ProcedureDTO request) {
        return accepted("La creación del procedimiento fue iniciada", procedureService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateProcedure(
            @PathVariable final UUID id,
            @RequestBody final ProcedureDTO request) {
        return accepted("La actualización del procedimiento fue iniciada", procedureService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateProcedure(@PathVariable final UUID id) {
        return accepted("La desactivación del procedimiento fue iniciada", procedureService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> activateProcedure(@PathVariable final UUID id) {
        return accepted("La activación del procedimiento fue iniciada", procedureService.activate(id));
    }

    private ResponseEntity<Map<String, String>> accepted(final String message, final String idMessage) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", message, "idMessage", idMessage));
    }
}
