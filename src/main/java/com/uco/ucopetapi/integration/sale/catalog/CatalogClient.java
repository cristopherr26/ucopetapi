package com.uco.ucopetapi.integration.sale.catalog;

import java.util.UUID;

public interface CatalogClient {

    CatalogItemResponse getProduct(UUID productId, UUID headquarterId);

    CatalogItemResponse getService(UUID serviceId);
}