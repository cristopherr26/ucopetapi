package com.uco.ucopetapi.controllers.petCare;

import com.uco.ucopetapi.dto.attention.CancellationRequest;
import com.uco.ucopetapi.dto.medicationOrder.CreateMedicationOrderRequest;
import com.uco.ucopetapi.dto.medicationOrder.MedicationOrderDTO;
import com.uco.ucopetapi.dto.medicationOrder.SignatureVerificationDTO;
import com.uco.ucopetapi.service.medicationOrder.MedicationOrderService;
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
public class MedicationOrderController {

    private final MedicationOrderService medicationOrderService;

    public MedicationOrderController(final MedicationOrderService medicationOrderService) {
        this.medicationOrderService = medicationOrderService;
    }

    @PostMapping("/{petCareId}/medication-orders")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicationOrderDTO> issue(@PathVariable final UUID petCareId,
                                                    @RequestBody final CreateMedicationOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicationOrderService.issue(petCareId, request));
    }

    @GetMapping("/{petCareId}/medication-orders")
    public ResponseEntity<List<MedicationOrderDTO>> findByPetCare(@PathVariable final UUID petCareId) {
        return ResponseEntity.ok(medicationOrderService.findByPetCare(petCareId));
    }

    @GetMapping("/medication-orders/{orderId}")
    public ResponseEntity<MedicationOrderDTO> findById(@PathVariable final UUID orderId) {
        return ResponseEntity.ok(medicationOrderService.findById(orderId));
    }

    @GetMapping("/medication-orders/pet/{petId}")
    public ResponseEntity<List<MedicationOrderDTO>> findByPet(@PathVariable final UUID petId) {
        return ResponseEntity.ok(medicationOrderService.findByPet(petId));
    }

    @GetMapping("/medication-orders/{orderId}/verify")
    public ResponseEntity<SignatureVerificationDTO> verify(@PathVariable final UUID orderId) {
        return ResponseEntity.ok(medicationOrderService.verifySignature(orderId));
    }

    @PatchMapping("/medication-orders/{orderId}/cancel")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicationOrderDTO> cancel(@PathVariable final UUID orderId,
                                                     @RequestBody final CancellationRequest request) {
        return ResponseEntity.ok(medicationOrderService.cancel(orderId, request));
    }
}
