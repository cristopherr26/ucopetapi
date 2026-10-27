package com.uco.ucopetapi.dto.product;

import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.UUID;

public record CatalogItemDTO(
        UUID id,
        CatalogItemType type,
        String name,
        String description,
        String imageUrl,
        Integer price,
        TaxCategory taxCategory,
        Boolean sellable,
        Boolean active,
        String category,
        Boolean purchasable,
        List<UUID> headquarterIds,
        Integer stockAtLocation) {

    public static CatalogItemDTO of(ProductDTO product) {
        return new CatalogItemDTO(product.getId(), CatalogItemType.PRODUCT, product.getName(),
                product.getDescription(), product.getImageUrl(), product.getPrice(), product.getTaxCategory(),
                product.getSellable(), product.getActive(), product.getCategory().name(), null, null,
                product.getStockAtLocation());
    }

    public static CatalogItemDTO of(ServiceDTO service) {
        return new CatalogItemDTO(service.getId(), CatalogItemType.SERVICE, service.getName(),
                service.getDescription(), service.getImageUrl(), service.getPrice(), service.getTaxCategory(),
                service.getSellable(), service.getActive(), service.getCategory().name(), service.getPurchasable(),
                service.getHeadquarterIds(), null);
    }
}
