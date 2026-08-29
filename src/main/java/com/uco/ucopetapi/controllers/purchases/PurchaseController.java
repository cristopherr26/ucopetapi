package com.uco.ucopetapi.controllers.purchases;

import com.uco.ucopetapi.dto.purchases.LinkExpenseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.Item;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseStatus;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/purchases")
public class PurchaseController {

    @PostMapping
    public ResponseEntity<PurchaseResponseDTO> create(@RequestBody @Valid PurchaseRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(buildDummyPurchase(UUID.randomUUID()));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseResponseDTO>> findByFilter(
            @RequestParam(required = false) PurchaseStatus status,
            @RequestParam(required = false) UUID supplierId
    ) {
        return ResponseEntity.ok(List.of(buildDummyPurchase(UUID.randomUUID())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildDummyPurchase(id));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponseDTO> receive(@PathVariable UUID id) {
        return ResponseEntity.ok(buildDummyPurchase(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PurchaseResponseDTO> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(buildDummyPurchase(id));
    }

    @PostMapping("/{id}/link-expense")
    public ResponseEntity<PurchaseResponseDTO> linkExpense(
            @PathVariable UUID id,
            @RequestBody @Valid LinkExpenseRequestDTO request
    ) {
        return ResponseEntity.ok(buildDummyPurchase(id));
    }

    private PurchaseResponseDTO buildDummyPurchase(UUID id) {
        RelatedEntityDTO supplier = new RelatedEntityDTO(UUID.randomUUID(), "Distribuidora Veterinaria del Sur");
        RelatedEntityDTO product = new RelatedEntityDTO(UUID.randomUUID(), "Vacuna Sextuple canina");

        Integer quantity = 10;
        BigDecimal unitPrice = new BigDecimal("25000.00");
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        List<Item> items = List.of(new Item(product, quantity, unitPrice, subtotal));

        return new PurchaseResponseDTO(
                id,
                supplier,
                items,
                subtotal,
                PurchaseStatus.PENDING,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
