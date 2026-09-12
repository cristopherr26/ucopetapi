package com.uco.ucopetapi.controllers.purchases;

import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.purchases.LinkExpenseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.service.purchases.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponseDTO> create(@RequestBody @Valid PurchaseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createPurchase(request));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseResponseDTO>> findByFilter(
            @RequestParam(required = true) UUID headquarterId,
            @RequestParam(required = false) PurchaseStatus status,
            @RequestParam(required = false) UUID supplierId
    ) {
        return ResponseEntity.ok(purchaseService.listPurchases(headquarterId, status, supplierId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponseDTO> receive(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseService.receivePurchase(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PurchaseResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseService.cancelPurchase(id));
    }

    @PostMapping("/{id}/link-expense")
    public ResponseEntity<PurchaseResponseDTO> linkExpense(
            @PathVariable UUID id,
            @RequestBody @Valid LinkExpenseRequestDTO request
    ) {
        return ResponseEntity.ok(purchaseService.linkExpense(id, request));
    }
}
