package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.service.product.CatalogService;
import com.uco.ucopetapi.service.product.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final CatalogService catalogService;

    public ProductServiceImpl(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public List<ProductDTO> list(ProductCategory category, Boolean active, Boolean sellable, TaxCategory taxCategory, UUID headquarterId) {
        return catalogService.list(CatalogItemType.PRODUCT, toCatalogCategory(category), active, null, sellable,
                        taxCategory, headquarterId).stream()
                .map(ProductServiceImpl::toDto)
                .toList();
    }

    @Override
    public ProductDTO getById(UUID id, UUID headquarterId) {
        return toDto(catalogService.getById(CatalogItemType.PRODUCT, id, headquarterId));
    }

    @Override
    public ProductDTO create(ProductDTO request) {
        return toDto(catalogService.create(CatalogItemType.PRODUCT, toCatalogItem(request)));
    }

    @Override
    public ProductDTO update(UUID id, ProductDTO request) {
        return toDto(catalogService.update(CatalogItemType.PRODUCT, id, toCatalogItem(request)));
    }

    @Override
    public void deactivate(UUID id) {
        catalogService.deactivate(CatalogItemType.PRODUCT, id);
    }

    private static CatalogItemDTO toCatalogItem(ProductDTO request) {
        return CatalogItemDTO.builder()
                .id(request.getId())
                .type(CatalogItemType.PRODUCT)
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .price(request.getPrice() == null ? null : BigDecimal.valueOf(request.getPrice()))
                .taxCategory(request.getTaxCategory())
                .sellable(request.getSellable())
                .active(request.getActive())
                .category(toCatalogCategory(request.getCategory()))
                .build();
    }

    private static ProductDTO toDto(CatalogItemDTO item) {
        return new ProductDTO(item.getId(), item.getName(), item.getDescription(), item.getImageUrl(),
                item.getPrice() == null ? null : item.getPrice().intValue(), item.getTaxCategory(),
                item.getSellable(), item.getActive(),
                item.getCategory() == null ? null : ProductCategory.valueOf(item.getCategory().name()),
                List.of(), item.getStockAtLocation());
    }

    private static CatalogCategory toCatalogCategory(ProductCategory category) {
        return category == null ? null : CatalogCategory.valueOf(category.name());
    }
}
