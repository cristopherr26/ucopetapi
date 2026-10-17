package com.uco.ucopetapi.controllers.attentionhealth;

import com.uco.ucopetapi.dto.attentionhealth.AttentionHealthDTO;
import com.uco.ucopetapi.service.attentionhealth.AttentionHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * HTTP entry point for the new veterinary attention functionality.
 *
 * <p>This controller is intentionally isolated from the legacy attention
 * implementation. Business rules remain delegated to the service layer.</p>
 */
@RestController
@RequestMapping("/api/v1/attentionhealth")
public class AttentionHealthController {

    private final AttentionHealthService attentionHealthService;

    public AttentionHealthController(final AttentionHealthService attentionHealthService) {
        this.attentionHealthService = attentionHealthService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AttentionHealthDTO> createAttention(
            @RequestBody final AttentionHealthDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attentionHealthService.create(request));
    }

    @GetMapping("/{attentionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttentionHealthDTO> findAttentionById(
            @PathVariable final Long attentionId) {
        return ResponseEntity.ok(attentionHealthService.findById(attentionId));
    }

    @GetMapping("/pet/{petId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttentionHealthDTO>> findAttentionHistoryByPet(
            @PathVariable final UUID petId) {
        return ResponseEntity.ok(attentionHealthService.findByPetId(petId));
    }

    @PutMapping("/{attentionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AttentionHealthDTO> updateAttention(
            @PathVariable final Long attentionId,
            @RequestBody final AttentionHealthDTO request) {
        return ResponseEntity.ok(attentionHealthService.update(attentionId, request));
    }

    @PatchMapping("/{attentionId}/start")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AttentionHealthDTO> startAttention(
            @PathVariable final Long attentionId) {
        return ResponseEntity.ok(attentionHealthService.start(attentionId));
    }

    @PatchMapping("/{attentionId}/cancel")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AttentionHealthDTO> cancelAttention(
            @PathVariable final Long attentionId) {
        return ResponseEntity.ok(attentionHealthService.cancel(attentionId));
    }

    @PatchMapping("/{attentionId}/finish")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AttentionHealthDTO> finishAttention(
            @PathVariable final Long attentionId) {
        return ResponseEntity.ok(attentionHealthService.finish(attentionId));
    }
}


