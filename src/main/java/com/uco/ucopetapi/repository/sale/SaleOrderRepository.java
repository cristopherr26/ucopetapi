package com.uco.ucopetapi.repository.sale;

import com.uco.ucopetapi.domain.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleOrderRepository {

    SaleOrderDomain save(SaleOrderDomain saleOrderDomain);

    Optional<SaleOrderDomain> findById(UUID id);

    boolean existsById(UUID id);

    List<SaleOrderDomain> findAll();

    List<SaleOrderDomain> findByFilter(SaleOrderDTO filter, LocalDate dateFrom, LocalDate dateTo);

    Optional<SaleOrderDomain> findLastOrderNumberByHeadquarter(UUID headquarterId);

}
