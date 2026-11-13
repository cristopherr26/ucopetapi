package com.uco.ucopetapi.service.sale.sale;

import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SaleOrderService {

    SaleOrderDomain registerNewSaleOrder(SaleOrderDomain saleOrder);

    SaleOrderDomain editExistentSaleOrder(UUID id, SaleOrderDomain saleOrder);

    SaleOrderDomain cancelSaleOrder(UUID id);

    List<SaleOrderDomain> findSaleOrderByFilter(
            SaleOrderDomain filter,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    List<SaleOrderDomain> findAll();
}
