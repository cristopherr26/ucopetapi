package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.dto.product.StockDTO;

import java.util.List;
import java.util.UUID;

public interface StockService {

    StockDTO adjustStock(UUID productId, UUID headquarterId, Integer quantity);

    StockDTO findByProductAndHeadquarter(UUID productId, UUID headquarterId);

    List<StockDTO> findByProduct(UUID productId);
}