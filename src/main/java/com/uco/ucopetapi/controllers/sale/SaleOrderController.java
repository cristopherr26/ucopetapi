package com.uco.ucopetapi.controllers.sale;

import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SaleOrderController {

    ResponseEntity<SaleOrderDTO> registerNewSaleOrder(SaleOrderDTO saleOrderDTO);

    ResponseEntity<SaleOrderDTO> editExistentSaleOrder(UUID id, SaleOrderDTO saleOrderDTO);

    ResponseEntity<SaleOrderDTO> cancelSaleOrder(UUID id);

    ResponseEntity<List<SaleOrderDTO>> findSaleOrderByFilter(UUID headquarterId, UUID clientID, UUID petId, UUID healthPlanId, String orderNumber, SaleOrderState state, LocalDate dateFrom, LocalDate dateTo);

    ResponseEntity<List<SaleOrderDTO>> findAll();
}

