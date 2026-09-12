package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.domain.product.ProductStatus;
import com.uco.ucopetapi.domain.product.ProductType;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
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
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(productService.list(type, status, category, headquarterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(productService.getById(id, headquarterId));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(
            @PathVariable UUID id,
            @RequestBody ProductDTO request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidData(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}