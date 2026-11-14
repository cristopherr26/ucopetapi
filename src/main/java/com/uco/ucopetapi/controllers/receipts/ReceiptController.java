package com.uco.ucopetapi.controllers.receipts;

import com.uco.ucopetapi.dto.receipts.ReceiptAcceptedDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptRequestDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptResponseDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptStatus;
import com.uco.ucopetapi.service.receipts.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ReceiptResponseDTO> findById(@PathVariable final UUID id) {
        return ResponseEntity.ok(receiptService.findById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ReceiptResponseDTO>> findByFilter(
            @RequestParam(required = false) final UUID tutorId,
            @RequestParam(required = false) final ReceiptStatus state) {
        return ResponseEntity.ok(receiptService.findByFilter(tutorId, state));
    }

    @GetMapping("/daily-total")
    public ResponseEntity<BigDecimal> dailyTotal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime endDate) {
        return ResponseEntity.ok(receiptService.dailyTotal(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<ReceiptAcceptedDTO> createNewReceipt(@RequestBody @Valid final ReceiptRequestDTO request) {
        return ResponseEntity.accepted().body(receiptService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptAcceptedDTO> updateReceipt(
            @PathVariable final UUID id,
            @RequestBody @Valid final ReceiptRequestDTO request) {
        return ResponseEntity.accepted().body(receiptService.update(id, request));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReceiptAcceptedDTO> cancelReceipt(@PathVariable final UUID id) {
        return ResponseEntity.accepted().body(receiptService.cancel(id));
    }
}