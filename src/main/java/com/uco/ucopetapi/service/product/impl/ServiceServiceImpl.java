package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.service.product.CatalogService;
import com.uco.ucopetapi.service.product.ServiceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceServiceImpl implements ServiceService {

    private final CatalogService catalogService;

    public ServiceServiceImpl(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public List<ServiceDTO> list(ServiceCategory category, Boolean active, Boolean purchasable, Boolean sellable,
                                 TaxCategory taxCategory, UUID headquarterId) {
        return catalogService.list(CatalogItemType.SERVICE, toCatalogCategory(category), active, purchasable, sellable,
                        taxCategory, headquarterId).stream()
                .map(ServiceServiceImpl::toDto)
                .toList();
    }

    @Override
    public ServiceDTO getById(UUID id) {
        return toDto(catalogService.getById(CatalogItemType.SERVICE, id, null));
    }

    @Override
    public ServiceDTO create(ServiceDTO request) {
        return toDto(catalogService.create(CatalogItemType.SERVICE, toCatalogItem(request)));
    }

    @Override
    public ServiceDTO update(UUID id, ServiceDTO request) {
        return toDto(catalogService.update(CatalogItemType.SERVICE, id, toCatalogItem(request)));
    }

    @Override
    public void deactivate(UUID id) {
        catalogService.deactivate(CatalogItemType.SERVICE, id);
    }

    @Override
    public boolean isActive(UUID id) {
        return catalogService.isActive(id);
    }

    @Override
    public long countActiveServices() {
        return catalogService.countActiveServices();
    }

    private static CatalogItemDTO toCatalogItem(ServiceDTO request) {
        return CatalogItemDTO.builder()
                .id(request.getId())
                .type(CatalogItemType.SERVICE)
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .price(request.getPrice())
                .taxCategory(request.getTaxCategory())
                .sellable(request.getSellable())
                .active(request.getActive())
                .category(toCatalogCategory(request.getCategory()))
                .purchasable(request.getPurchasable())
                .headquarterIds(request.getHeadquarterIds())
                .build();
    }

    private static ServiceDTO toDto(CatalogItemDTO item) {
        return new ServiceDTO(item.getId(), item.getName(), item.getDescription(), item.getImageUrl(),
                item.getPrice(), item.getTaxCategory(), item.getSellable(), item.getActive(),
                item.getCategory() == null ? null : ServiceCategory.valueOf(item.getCategory().name()),
                item.getPurchasable(), List.of(), item.getHeadquarterIds());
    }

    private static CatalogCategory toCatalogCategory(ServiceCategory category) {
        return category == null ? null : CatalogCategory.valueOf(category.name());
    }
}
