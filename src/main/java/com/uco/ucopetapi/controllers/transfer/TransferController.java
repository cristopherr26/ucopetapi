package com.uco.ucopetapi.controllers.transfer;

import com.uco.ucopetapi.dto.transfers.TransferRequestDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createNewTransfer(@Valid @RequestBody TransferRequestDTO transfer) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    public ResponseEntity<List<TransferResponseDTO>> findByFilter(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) UUID originHeadquarterId,
            @RequestParam(required = false) UUID destinationHeadquarterId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> updateTransfer(
            @PathVariable UUID id,
            @Valid @RequestBody TransferRequestDTO transfer
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TransferResponseDTO> updateTransferStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TransferStatusUpdateDTO statusUpdate
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTransfer(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}