package com.uco.ucopetapi.controllers.receipts;

import com.uco.ucopetapi.dto.receipts.ReceiptDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/receipts")
public class ReceiptController {

    // TODO: inject ReceiptService once the service/repository layer exists
    // private final ReceiptService receiptService;
    // public ReceiptController(ReceiptService receiptService) {
    //     this.receiptService = receiptService;
    // }

    @GetMapping
    public ResponseEntity<List<ReceiptDTO>> findAllReceipts() {
        // TODO: replace with receiptService.findAll()
        return ResponseEntity.ok(List.of(buildSampleReceipt()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptDTO> findById(@PathVariable UUID id) {
        // TODO: replace with receiptService.findById(id)
        return ResponseEntity.ok(buildSampleReceipt());
    }

    @PostMapping
    public ResponseEntity<ReceiptDTO> createNewReceipt(@RequestBody ReceiptDTO receipt) {
        // TODO: replace with receiptService.create(receipt)
        return ResponseEntity.status(HttpStatus.CREATED).body(buildSampleReceipt());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptDTO> updateReceipt(
            @PathVariable UUID id,
            @RequestBody ReceiptDTO receipt
    ) {
        // TODO: replace with receiptService.update(id, receipt)
        return ResponseEntity.ok(buildSampleReceipt());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceipt(@PathVariable UUID id) {
        // TODO: replace with receiptService.delete(id)
        return ResponseEntity.noContent().build();
    }

    
    private ReceiptDTO buildSampleReceipt() {
        return new ReceiptDTO(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "REC-000123",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "Consulta veterinaria",
                50000.0,
                "CASH",
                LocalDateTime.of(2026, 8, 22, 10, 30),
                "ACTIVE"
        );
    }
}