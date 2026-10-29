package com.uco.ucopetapi.repository.sale.itemSale;

import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemSaleRepository {

    ItemSaleDomain save(ItemSaleDomain itemSaleDomain);

    Optional<ItemSaleDomain> findById(UUID id);

    List<ItemSaleDomain> findBySaleOrderId(UUID saleOrderId);

    void deleteById(UUID id);

    boolean existsById(UUID id);

}
