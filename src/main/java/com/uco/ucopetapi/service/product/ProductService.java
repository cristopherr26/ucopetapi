package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.ProductStatus;
import com.uco.ucopetapi.domain.product.ProductType;
import com.uco.ucopetapi.dto.product.ProductDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    List<ProductDTO> list(ProductType type, ProductStatus status, String category, UUID headquarterId);

    ProductDTO getById(UUID id, UUID headquarterId);

    ProductDTO create(ProductDTO request);

    ProductDTO update(UUID id, ProductDTO request);

    void deactivate(UUID id);
}