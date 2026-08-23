package com.uco.ucopetapi.controllers.receipts;

import com.uco.ucopetapi.dto.receipts.ReceiptDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<ReceiptDTO> createNewReceipt(@RequestBody ReceiptDTO receipt) {
        // TODO: delegate to receiptService.create(receipt)
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @GetMapping
    public ResponseEntity<List<ReceiptDTO>> findAllReceipts() {
        // TODO: delegate to receiptService.findAll()
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ReceiptDTO>> findReceiptsByFilter(
            @RequestParam(required = false) UUID tutorId,
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String paymentMethod
    ) {
        // TODO: delegate to receiptService.findByFilter(tutorId, petId, state, paymentMethod)
        return ResponseEntity.ok(List.of());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptDTO> updateReceipt(
            @PathVariable UUID id,
            @RequestBody ReceiptDTO receipt
    ) {
        // TODO: delegate to receiptService.update(id, receipt)
        return ResponseEntity.ok(receipt);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReceipt(
            @PathVariable UUID id,
            @RequestBody CancelReceiptRequest request
    ) {
        // TODO: delegate to receiptService.cancel(id, request.isCancelled())
        return ResponseEntity.noContent().build();
    }

    public record CancelReceiptRequest(Boolean isCancelled) {
    }
}