package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.service.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<ProductDTO> list(@RequestParam(required = false) ProductCategory category,
                                 @RequestParam(required = false) Boolean active,
                                 @RequestParam(required = false) Boolean sellable,
                                 @RequestParam(required = false) TaxCategory taxCategory,
                                 @RequestParam(required = false) UUID headquarterId) {
        return productService.list(category, active, sellable, taxCategory, headquarterId);
    }

    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable UUID id, @RequestParam(required = false) UUID headquarterId) {
        return productService.getById(id, headquarterId);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> create(@Valid @RequestBody ProductDTO product) {
        return new ResponseEntity<>(productService.create(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable UUID id, @Valid @RequestBody ProductDTO product) {
        return new ResponseEntity<>(productService.update(id, product), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
