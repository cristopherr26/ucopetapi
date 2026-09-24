package com.uco.ucopetapi.controllers.petCare;

import com.uco.ucopetapi.dto.attention.AttentionResponse;
import com.uco.ucopetapi.dto.attention.FinishAttentionRequest;
import com.uco.ucopetapi.dto.attention.StartAttentionRequest;
import com.uco.ucopetapi.service.attention.AppointmentAttentionOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/petcares")
@PreAuthorize("hasRole('DOCTOR')")
public class AttentionController {

    private final AppointmentAttentionOrchestrator orchestrator;

    public AttentionController(final AppointmentAttentionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/appointments/{appointmentId}/start")
    public ResponseEntity<AttentionResponse> start(@PathVariable final UUID appointmentId,
                                                   @RequestBody final StartAttentionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orchestrator.start(appointmentId, request));
    }

    @PostMapping("/{petCareId}/finish")
    public ResponseEntity<AttentionResponse> finish(@PathVariable final UUID petCareId,
                                                    @RequestBody final FinishAttentionRequest request) {
        return ResponseEntity.ok(orchestrator.finish(petCareId, request));
    }
}

