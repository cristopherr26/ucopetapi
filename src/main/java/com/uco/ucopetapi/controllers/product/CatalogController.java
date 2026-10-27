package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.product.ServiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final ProductService productService;
    private final ServiceService serviceService;

    public CatalogController(ProductService productService, ServiceService serviceService) {
        this.productService = productService;
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<CatalogItemDTO> list(@RequestParam(required = false) CatalogItemType type,
                                     @RequestParam(required = false) Boolean active,
                                     @RequestParam(required = false) Boolean sellable,
                                     @RequestParam(required = false) TaxCategory taxCategory,
                                     @RequestParam(required = false) UUID headquarterId) {
        Stream<CatalogItemDTO> products = type == CatalogItemType.SERVICE ? Stream.empty()
                : productService.list(null, active, sellable, taxCategory, headquarterId).stream()
                        .map(CatalogItemDTO::of);
        Stream<CatalogItemDTO> services = type == CatalogItemType.PRODUCT ? Stream.empty()
                : serviceService.list(null, active, null, sellable, taxCategory, headquarterId).stream()
                        .map(CatalogItemDTO::of);
        return Stream.concat(products, services)
                .sorted(Comparator.comparing(CatalogItemDTO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
