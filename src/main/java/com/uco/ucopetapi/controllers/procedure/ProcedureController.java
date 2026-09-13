package com.uco.ucopetapi.controllers.procedure;

import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.service.procedure.ProcedureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<ProcedureDTO> findProcedureById(@PathVariable final UUID id) {
        return ResponseEntity.ok(procedureService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProcedureDTO> createProcedure(@Valid @RequestBody final ProcedureDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(procedureService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcedureDTO> updateProcedure(
            @PathVariable final UUID id,
            @Valid @RequestBody final ProcedureDTO request) {
        return ResponseEntity.ok(procedureService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ProcedureDTO> deactivateProcedure(@PathVariable final UUID id) {
        return ResponseEntity.ok(procedureService.deactivate(id));
    }
}
