package com.uco.ucopetapi.controllers.sale.sale.impl;

import com.uco.ucopetapi.controllers.sale.sale.SaleOrderController;
import com.uco.ucopetapi.domain.sale.enums.SaleOrderState;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.itemSale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.sale.SaleOrderDTO;
import com.uco.ucopetapi.service.sale.sale.SaleOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
public class SaleOrderControllerImpl implements SaleOrderController {

    private final SaleOrderService saleOrderService;

    public SaleOrderControllerImpl(SaleOrderService saleOrderService) {
        this.saleOrderService = saleOrderService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<SaleOrderDTO>> findSaleOrderByFilter(
            @RequestParam(required = false) UUID headquarterId,
            @RequestParam(required = false) UUID clientID,
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) UUID healthPlanId,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) SaleOrderState state,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        SaleOrderDomain filter = new SaleOrderDomain(
                null,
                headquarterId,
                orderNumber,
                null,
                clientID,
                petId,
                healthPlanId,
                null,
                null,
                null,
                null,
                null,
                state
        );

        List<SaleOrderDTO> response = saleOrderService
                .findSaleOrderByFilter(filter, dateFrom, dateTo)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<SaleOrderDTO> registerNewSaleOrder(
            @RequestBody SaleOrderDTO saleOrderDTO) {

        SaleOrderDomain saleOrder = toDomain(saleOrderDTO);

        SaleOrderDTO created = toDTO(
                saleOrderService.registerNewSaleOrder(saleOrder)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<SaleOrderDTO> editExistentSaleOrder(
            @PathVariable UUID id,
            @RequestBody SaleOrderDTO saleOrderDTO) {

        SaleOrderDomain saleOrder = toDomain(saleOrderDTO);

        SaleOrderDTO updated = toDTO(
                saleOrderService.editExistentSaleOrder(id, saleOrder)
        );

        return ResponseEntity.ok(updated);
    }

    @Override
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SaleOrderDTO> cancelSaleOrder(
            @PathVariable UUID id) {

        SaleOrderDTO cancelled = toDTO(
                saleOrderService.cancelSaleOrder(id)
        );

        return ResponseEntity.ok(cancelled);
    }

    private SaleOrderDomain toDomain(SaleOrderDTO dto) {
        return new SaleOrderDomain(
                dto.getId(),
                dto.getHeadquarterId(),
                dto.getOrderNumber(),
                dto.getSaleDate(),
                dto.getClientID(),
                dto.getPetId(),
                dto.getHealthPlanId(),
                dto.getSubtotal(),
                dto.getTotalDiscount(),
                dto.getTotalTaxes(),
                dto.getTotal(),
                dto.getComment(),
                dto.getState()
        );
    }

    private SaleOrderDTO toDTO(SaleOrderDomain domain) {
        SaleOrderDTO dto = new SaleOrderDTO(
                domain.getId(),
                domain.getHeadquarterId(),
                domain.getOrderNumber(),
                domain.getSaleDate(),
                domain.getClientID(),
                domain.getPetId(),
                domain.getHealthPlanId(),
                domain.getSubtotal(),
                domain.getTotalDiscount(),
                domain.getTotalTaxes(),
                domain.getTotal(),
                domain.getComment(),
                domain.getState()
        );

        List<ItemSaleDTO> items = domain.getItems()
                .stream()
                .map(item -> new ItemSaleDTO(
                        item.getId(),
                        item.getSaleOrderId(),
                        item.getItemId(),
                        item.getItemType(),
                        item.getTaxCategory(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        dto.setItems(items);

        return dto;
    }
}
