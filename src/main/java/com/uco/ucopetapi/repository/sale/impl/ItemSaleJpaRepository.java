package com.uco.ucopetapi.repository.sale.impl;

import com.uco.ucopetapi.domain.sale.ItemSaleDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ItemSaleJpaRepository extends JpaRepository<ItemSaleDomain, UUID> {

    List<ItemSaleDomain> findBySaleOrderId(UUID saleOrderId);
}
