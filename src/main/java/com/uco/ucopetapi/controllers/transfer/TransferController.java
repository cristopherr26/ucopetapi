package com.uco.ucopetapi.controllers.transfer;

import com.uco.ucopetapi.dto.transfers.TransferRequestDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import com.uco.ucopetapi.dto.transfers.TransferStatusUpdateDTO;
import com.uco.ucopetapi.service.transfer.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createNewTransfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequestDTO transfer) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        TransferResponseDTO created = transferService.createTransfer(transfer, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TransferResponseDTO>> findByFilter(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) UUID originHeadquarterId,
            @RequestParam(required = false) UUID destinationHeadquarterId) {
        return ResponseEntity.ok(transferService.findByFilter(status, originHeadquarterId, destinationHeadquarterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> updateTransfer(
            @PathVariable UUID id,
            @Valid @RequestBody TransferRequestDTO transfer) {
        return ResponseEntity.ok(transferService.updateTransfer(id, transfer));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TransferResponseDTO> updateTransferStatus(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody TransferStatusUpdateDTO statusUpdate) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        TransferResponseDTO updated = transferService.updateTransferStatus(id, statusUpdate.status(), currentUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransfer(Authentication authentication, @PathVariable UUID id) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        transferService.cancelTransfer(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}