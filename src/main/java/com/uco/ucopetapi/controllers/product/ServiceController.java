package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnCreate;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnUpdate;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.service.product.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;


import jakarta.validation.groups.Default;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> list(
            @RequestParam(required = false) ServiceCategory category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean purchasable,
            @RequestParam(required = false) Boolean sellable,
            @RequestParam(required = false) TaxCategory taxCategory,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(serviceService.list(category, onlyActiveByDefault(active), purchasable,
                sellable, taxCategory, headquarterId));
    }

    private static Boolean onlyActiveByDefault(Boolean active) {
        return active == null ? Boolean.TRUE : active;
    }

    @GetMapping("/count-active")
    public ResponseEntity<Long> countActiveServices() {
        return ResponseEntity.ok(
                serviceService.countActiveServices()
        );
    }

    @GetMapping("/{id}/active")
    public ResponseEntity<Boolean> isActive(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                serviceService.isActive(id)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceDTO> create(
            @Validated({IOnCreate.class, Default.class}) @RequestBody ServiceDTO request) {
        ServiceDTO created = serviceService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> update(@PathVariable UUID id,
            @Validated({IOnUpdate.class, Default.class}) @RequestBody ServiceDTO request) {
        return ResponseEntity.ok(serviceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        serviceService.deactivate(id);
        return ResponseEntity.noContent().build();
    }


}