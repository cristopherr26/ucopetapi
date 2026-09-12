package com.uco.ucopetapi.controllers.receipts;

import com.uco.ucopetapi.dto.receipts.ReceiptRequestDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptResponseDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receipts")
public class ReceiptController {

    @GetMapping
    public ResponseEntity<List<ReceiptResponseDTO>> findAllReceipts() {
        return ResponseEntity.ok(List.of(exampleReceipt(UUID.randomUUID())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(exampleReceipt(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ReceiptResponseDTO>> findByFilter(
            @RequestParam(required = false) UUID tutorId,
            @RequestParam(required = false) ReceiptStatus state
    ) {
        return ResponseEntity.ok(List.of(exampleReceipt(UUID.randomUUID())));
    }

    @PostMapping
    public ResponseEntity<ReceiptResponseDTO> createNewReceipt(@RequestBody @Valid ReceiptRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exampleReceipt(UUID.randomUUID()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptResponseDTO> updateReceipt(
            @PathVariable UUID id,
            @RequestBody @Valid ReceiptRequestDTO request
    ) {
        return ResponseEntity.ok(exampleReceipt(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReceiptResponseDTO> cancelReceipt(@PathVariable UUID id) {
        ReceiptResponseDTO cancelled = exampleReceipt(id);
        return ResponseEntity.ok(new ReceiptResponseDTO(
                cancelled.id(), cancelled.receiptNumber(), cancelled.tutorId(), cancelled.petId(),
                cancelled.concept(), cancelled.amount(), cancelled.payMethodId(), cancelled.payMethodName(),
                cancelled.date(), ReceiptStatus.CANCELLED
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceipt(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    private ReceiptResponseDTO exampleReceipt(UUID id) {
        return new ReceiptResponseDTO(
                id,
                "REC-000123",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "Consulta veterinaria",
                new BigDecimal("50000.00"),
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "Efectivo",
                LocalDateTime.of(2026, 8, 22, 10, 30),
                ReceiptStatus.ACTIVE
        );
    }
}
