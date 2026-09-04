package com.uco.ucopetapi.controllers.procedure;

import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    private String buildProcedureCode(final UUID id) {
        return "PRC-" + id.toString().substring(0, 8).toUpperCase();
    }

    private ProcedureDTO buildProcedure(final UUID id, final String code, final String type,
                                        final String description, final Integer durationMinutes,
                                        final boolean isActive) {
        return new ProcedureDTO(id, code, type, description, durationMinutes, isActive);
    }

    @GetMapping
    public ResponseEntity<List<ProcedureDTO>> findAllProcedures() {
        List<ProcedureDTO> procedures = List.of(
                buildProcedure(
                        UUID.fromString("b1d8c2f4-7d3a-4c4c-9f3c-3fb7f0b9a001"),
                        "PRC-001",
                        "Vacunación",
                        "Aplicación de vacuna preventiva para mascotas.",
                        20,
                        true
                ),
                buildProcedure(
                        UUID.fromString("b1d8c2f4-7d3a-4c4c-9f3c-3fb7f0b9a002"),
                        "PRC-002",
                        "Consulta general",
                        "Valoración clínica general del paciente.",
                        30,
                        true
                )
        );
        return ResponseEntity.ok(procedures);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcedureDTO> findProcedureById(@PathVariable UUID id) {
        ProcedureDTO procedure = buildProcedure(
                id,
                "PRC-" + id.toString().substring(0, 8).toUpperCase(),
                "Consulta general",
                "Procedimiento consultado desde el catálogo.",
                30,
                true
        );
        return ResponseEntity.ok(procedure);
    }

    @PostMapping
    public ResponseEntity<ProcedureDTO> createProcedure(@RequestBody ProcedureDTO request) {
        UUID id = UUID.randomUUID();
        ProcedureDTO createdProcedure = buildProcedure(
                id,
                request != null && request.getCode() != null ? request.getCode() : buildProcedureCode(id),
                request != null && request.getType() != null ? request.getType() : "Procedimiento sin tipo",
                request != null && request.getDescription() != null ? request.getDescription() : "Descripción no definida",
                request != null && request.getDurationMinutes() != null ? request.getDurationMinutes() : 30,
                true
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProcedure);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcedureDTO> updateProcedure(
            @PathVariable UUID id,
            @RequestBody ProcedureDTO request) {

        ProcedureDTO updatedProcedure = buildProcedure(
                id,
                request != null && request.getCode() != null ? request.getCode() : buildProcedureCode(id),
                request != null && request.getType() != null ? request.getType() : "Procedimiento actualizado",
                request != null && request.getDescription() != null ? request.getDescription() : "Descripción actualizada del procedimiento.",
                request != null && request.getDurationMinutes() != null ? request.getDurationMinutes() : 30,
                request != null && request.isActive()
        );
        return ResponseEntity.ok(updatedProcedure);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcedure(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }
}
