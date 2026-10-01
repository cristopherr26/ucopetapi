package com.uco.ucopetapi.repository.sale.impl;

import com.uco.ucopetapi.domain.sale.SaleOrderDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SaleOrderJpaRepository extends JpaRepository<SaleOrderDomain, UUID>,
        JpaSpecificationExecutor<SaleOrderDomain> {

    Optional<SaleOrderDomain> findTopByHeadquarterIdOrderByOrderNumberDesc(UUID headquarterId);
}