package com.uco.ucopetapi.service.sale;

import com.uco.ucopetapi.dto.sale.SaleOrderDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SaleOrderService {

    SaleOrderDTO registerNewSaleOrder(SaleOrderDTO saleOrderDTO);

    SaleOrderDTO editExistentSaleOrder(UUID id, SaleOrderDTO saleOrderDTO);

    SaleOrderDTO cancelSaleOrder(UUID id);

    List<SaleOrderDTO> findSaleOrderByFilter(SaleOrderDTO filter, LocalDate dateFrom, LocalDate dateTo);

    List<SaleOrderDTO> findAll();
}
