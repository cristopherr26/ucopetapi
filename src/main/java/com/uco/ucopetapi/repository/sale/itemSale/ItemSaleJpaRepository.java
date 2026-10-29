package com.uco.ucopetapi.repository.sale.itemSale;

import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemSaleJpaRepository extends JpaRepository<ItemSaleDomain, UUID> {

    List<ItemSaleDomain> findBySaleOrderId(UUID saleOrderId);
}
