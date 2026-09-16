package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ProductDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    List<ProductDTO> list(ProductCategory category, Boolean active, Boolean sellable, TaxCategory taxCategory, UUID headquarterId);

    ProductDTO getById(UUID id, UUID headquarterId);

    ProductDTO create(ProductDTO request);

    ProductDTO update(UUID id, ProductDTO request);

    void deactivate(UUID id);
}