package com.uco.ucopetapi.controllers.petCare;

import com.uco.ucopetapi.dto.attention.CancellationRequest;
import com.uco.ucopetapi.dto.petCare.CreatePetCareRequestDTO;
import com.uco.ucopetapi.dto.petCare.PetCareRequestDTO;
import com.uco.ucopetapi.service.petCare.PetCareRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/petcares")
public class PetCareRequestController {

    private final PetCareRequestService petCareRequestService;

    public PetCareRequestController(final PetCareRequestService petCareRequestService) {
        this.petCareRequestService = petCareRequestService;
    }

    @PostMapping("/{petCareId}/requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<PetCareRequestDTO> create(@PathVariable final UUID petCareId,
                                                    @RequestBody final CreatePetCareRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petCareRequestService.create(petCareId, request));
    }

    @GetMapping("/{petCareId}/requests")
    public ResponseEntity<List<PetCareRequestDTO>> findByPetCare(@PathVariable final UUID petCareId) {
        return ResponseEntity.ok(petCareRequestService.findByPetCare(petCareId));
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<PetCareRequestDTO> cancel(@PathVariable final UUID requestId,
                                                    @RequestBody final CancellationRequest request) {
        return ResponseEntity.ok(petCareRequestService.cancel(requestId, request));
    }
}
