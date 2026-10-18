package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.service.product.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<List<CatalogItemDTO>> list(
            @RequestParam(required = false) CatalogCategory category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean sellable,
            @RequestParam(required = false) TaxCategory taxCategory,
            @RequestParam(required = false) UUID headquarterId
    ) {
        return ResponseEntity.ok(catalogService.listAll(category, active == null ? Boolean.TRUE : active,
                sellable, taxCategory, headquarterId));
    }
}
