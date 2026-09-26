package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnCreate;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnUpdate;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.service.product.ProductService;
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

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> list(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean sellable,
            @RequestParam(required = false) TaxCategory taxCategory,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(productService.list(category, onlyActiveByDefault(active), sellable,
                taxCategory, headquarterId));
    }

    private static Boolean onlyActiveByDefault(Boolean active) {
        return active == null ? Boolean.TRUE : active;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(productService.getById(id, headquarterId));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> create(
            @Validated({IOnCreate.class, Default.class}) @RequestBody ProductDTO request) {
        ProductDTO created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable UUID id,
            @Validated({IOnUpdate.class, Default.class}) @RequestBody ProductDTO request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

}