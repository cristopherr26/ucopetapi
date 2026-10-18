package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.config.InventoryQueueConfig;
import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnCreate;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnUpdate;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.service.product.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;


import jakarta.validation.groups.Default;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final String BY_ID = "/{id}";

    private final CatalogService catalogService;
    private final InventoryQueueConfig queueConfig;

    public ProductController(CatalogService catalogService, InventoryQueueConfig queueConfig) {
        this.catalogService = catalogService;
        this.queueConfig = queueConfig;
    }

    @GetMapping
    public ResponseEntity<List<CatalogItemDTO>> list(
            @RequestParam(required = false) CatalogCategory category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean sellable,
            @RequestParam(required = false) TaxCategory taxCategory,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(catalogService.list(CatalogItemType.PRODUCT, category, onlyActiveByDefault(active), null,
                sellable, taxCategory, headquarterId));
    }

    private static Boolean onlyActiveByDefault(Boolean active) {
        return active == null ? Boolean.TRUE : active;
    }

    @GetMapping(BY_ID)
    public ResponseEntity<CatalogItemDTO> getById(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(catalogService.getById(CatalogItemType.PRODUCT, id, headquarterId));
    }

    @PostMapping
    public ResponseEntity<CatalogItemDTO> create(
            @Validated({IOnCreate.class, Default.class}) @RequestBody CatalogItemDTO request) {
        if (queueConfig.isAsyncWrites()) {
            CatalogItemDTO accepted = catalogService.submitCreate(CatalogItemType.PRODUCT, request);
            return ResponseEntity.accepted().location(locationOf(accepted.getId())).body(accepted);
        }
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, request);
        return ResponseEntity.created(locationOf(created.getId())).body(created);
    }

    @PutMapping(BY_ID)
    public ResponseEntity<CatalogItemDTO> update(@PathVariable UUID id,
            @Validated({IOnUpdate.class, Default.class}) @RequestBody CatalogItemDTO request) {
        if (queueConfig.isAsyncWrites()) {
            catalogService.submitUpdate(CatalogItemType.PRODUCT, id, request);
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.ok(catalogService.update(CatalogItemType.PRODUCT, id, request));
    }

    @DeleteMapping(BY_ID)
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        if (queueConfig.isAsyncWrites()) {
            catalogService.submitDeactivate(CatalogItemType.PRODUCT, id);
            return ResponseEntity.accepted().build();
        }
        catalogService.deactivate(CatalogItemType.PRODUCT, id);
        return ResponseEntity.noContent().build();
    }

    private static URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path(BY_ID)
                .buildAndExpand(id)
                .toUri();
    }

}