package com.uco.ucopetapi.controllers.receipts;

import com.uco.ucopetapi.dto.receipts.ReceiptRequestDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptResponseDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptStatus;
import com.uco.ucopetapi.service.receipts.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receipts")
@PreAuthorize("hasRole('ADMIN')")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(final ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public ResponseEntity<List<ReceiptResponseDTO>> findAllReceipts() {
        return ResponseEntity.ok(receiptService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(receiptService.findById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ReceiptResponseDTO>> findByFilter(
            @RequestParam(required = false) UUID tutorId,
            @RequestParam(required = false) ReceiptStatus state
    ) {
        return ResponseEntity.ok(receiptService.findByFilter(tutorId, state));
    }

    @GetMapping("/daily-total")
    public ResponseEntity<BigDecimal> dailyTotal(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate
    ) {
        return ResponseEntity.ok(receiptService.dailyTotal(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<ReceiptResponseDTO> createNewReceipt(@RequestBody @Valid ReceiptRequestDTO request) {
        ReceiptResponseDTO created = receiptService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptResponseDTO> updateReceipt(
            @PathVariable UUID id,
            @RequestBody @Valid ReceiptRequestDTO request
    ) {
        return ResponseEntity.ok(receiptService.update(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReceiptResponseDTO> cancelReceipt(@PathVariable UUID id) {
        return ResponseEntity.ok(receiptService.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceipt(@PathVariable UUID id) {
        receiptService.delete(id);
        return ResponseEntity.noContent().build();
    }
}