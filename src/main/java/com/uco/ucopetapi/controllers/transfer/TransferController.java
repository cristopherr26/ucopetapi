package com.uco.ucopetapi.controllers.transfer;

import com.uco.ucopetapi.dto.transfers.TransferRequestDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import com.uco.ucopetapi.dto.transfers.TransferStatusUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createNewTransfer(@Valid @RequestBody TransferRequestDTO transfer) {
        TransferResponseDTO mockTransfer = buildMockTransfer(UUID.randomUUID(), transfer, TransferStatus.PENDING);
        return ResponseEntity.status(HttpStatus.CREATED).body(mockTransfer);
    }

    @GetMapping
    public ResponseEntity<List<TransferResponseDTO>> findByFilter(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) UUID originHeadquarterId,
            @RequestParam(required = false) UUID destinationHeadquarterId
    ) {
        List<TransferResponseDTO> mockTransfers = List.of(
                buildMockTransfer(UUID.randomUUID(), null, TransferStatus.PENDING),
                buildMockTransfer(UUID.randomUUID(), null, TransferStatus.IN_PROGRESS),
                buildMockTransfer(UUID.randomUUID(), null, TransferStatus.COMPLETED)
        );

        List<TransferResponseDTO> filtered = mockTransfers.stream()
                .filter(t -> status == null || t.status() == status)
                .toList();

        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> findById(@PathVariable UUID id) {
        TransferResponseDTO mockTransfer = buildMockTransfer(id, null, TransferStatus.PENDING);
        return ResponseEntity.ok(mockTransfer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> updateTransfer(
            @PathVariable UUID id,
            @Valid @RequestBody TransferRequestDTO transfer
    ) {
        TransferResponseDTO mockTransfer = buildMockTransfer(id, transfer, TransferStatus.PENDING);
        return ResponseEntity.ok(mockTransfer);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TransferResponseDTO> updateTransferStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TransferStatusUpdateDTO statusUpdate
    ) {
        TransferResponseDTO mockTransfer = buildMockTransfer(id, null, statusUpdate.status());
        return ResponseEntity.ok(mockTransfer);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransfer(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    private TransferResponseDTO buildMockTransfer(UUID id, TransferRequestDTO source, TransferStatus status) {
        RelatedEntityDTO origin = new RelatedEntityDTO(
                source != null ? source.originHeadquarterId() : UUID.randomUUID(),
                "Sede Poblado"
        );
        RelatedEntityDTO destination = new RelatedEntityDTO(
                source != null ? source.destinationHeadquarterId() : UUID.randomUUID(),
                "Sede Belen"
        );
        RelatedEntityDTO product = new RelatedEntityDTO(
                source != null ? source.productId() : UUID.randomUUID(),
                "Vacuna Sextuple canina"
        );
        RelatedEntityDTO createdBy = new RelatedEntityDTO(UUID.randomUUID(), "Jorge Castaneda");
        RelatedEntityDTO updatedBy = status == TransferStatus.PENDING
                ? null
                : new RelatedEntityDTO(UUID.randomUUID(), "Laura Higuita");

        return new TransferResponseDTO(
                id,
                origin,
                destination,
                product,
                source != null ? source.quantity() : 40,
                status,
                source != null ? source.observations() : "Cadena de frio requerida",
                createdBy,
                LocalDateTime.now().minusHours(2),
                updatedBy,
                status == TransferStatus.PENDING ? null : LocalDateTime.now()
        );
    }
}