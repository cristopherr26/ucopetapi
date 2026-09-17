package com.uco.ucopetapi.controllers.provider;

import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.service.provider.ProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    public ResponseEntity<ProviderDTO> createNewProvider(@RequestBody ProviderDTO provider) {
        return ResponseEntity.status(HttpStatus.CREATED).body(providerService.create(provider));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProviderDTO> updateProvider(@PathVariable UUID id, @RequestBody ProviderDTO provider) {
        return ResponseEntity.ok(providerService.update(id, provider));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ProviderDTO> deactivateProvider(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.deactivate(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(providerService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProviderDTO>> findAll() {
        return ResponseEntity.ok(providerService.findAll());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<UUID>> findByProviderName(
            @RequestParam(required = false) String providerName) {

        return ResponseEntity.ok(providerService.findByProviderName(providerName));
    }
}