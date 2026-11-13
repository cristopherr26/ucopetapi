package com.uco.ucopetapi.controllers.sale.itemSale.impl;

import com.uco.ucopetapi.controllers.sale.itemSale.ItemSaleController;
import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDomain;
import com.uco.ucopetapi.domain.sale.sale.AddItemSaleRequestDomain;
import com.uco.ucopetapi.domain.sale.sale.EditItemSaleRequestDomain;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.itemSale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.sale.SaleOrderDTO;
import com.uco.ucopetapi.service.sale.itemSale.ItemSaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/{saleOrderId}/items")
public class ItemSaleControllerImpl implements ItemSaleController {

    private final ItemSaleService itemSaleService;

    public ItemSaleControllerImpl(ItemSaleService itemSaleService) {
        this.itemSaleService = itemSaleService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ItemSaleDTO>> listItems(
            @PathVariable UUID saleOrderId) {

        List<ItemSaleDTO> items = itemSaleService
                .listItemsBySaleOrder(saleOrderId)
                .stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(items);
    }

    @Override
    @PostMapping
    public ResponseEntity<SaleOrderDTO> addItem(
            @PathVariable UUID saleOrderId,
            @RequestBody AddItemSaleRequestDomain request) {

        SaleOrderDomain saleOrder =
                itemSaleService.addItemToSaleOrder(saleOrderId, request);

        return ResponseEntity.ok(toSaleOrderDTO(saleOrder));
    }

    @Override
    @PutMapping("/{itemSaleId}")
    public ResponseEntity<SaleOrderDTO> editItem(
            @PathVariable UUID saleOrderId,
            @PathVariable UUID itemSaleId,
            @RequestBody EditItemSaleRequestDomain request) {

        SaleOrderDomain saleOrder =
                itemSaleService.editItemInSaleOrder(
                        saleOrderId,
                        itemSaleId,
                        request
                );

        return ResponseEntity.ok(toSaleOrderDTO(saleOrder));
    }

    @Override
    @DeleteMapping("/{itemSaleId}")
    public ResponseEntity<SaleOrderDTO> removeItem(
            @PathVariable UUID saleOrderId,
            @PathVariable UUID itemSaleId) {

        SaleOrderDomain saleOrder =
                itemSaleService.removeItemFromSaleOrder(
                        saleOrderId,
                        itemSaleId
                );

        return ResponseEntity.ok(toSaleOrderDTO(saleOrder));
    }

    private ItemSaleDTO toDTO(ItemSaleDomain domain) {
        return new ItemSaleDTO(
                domain.getId(),
                domain.getSaleOrderId(),
                domain.getItemId(),
                domain.getItemType(),
                domain.getTaxCategory(),
                domain.getQuantity(),
                domain.getUnitPrice(),
                domain.getSubtotal()
        );
    }

    private SaleOrderDTO toSaleOrderDTO(SaleOrderDomain domain) {
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
                .map(this::toDTO)
                .toList();

        dto.setItems(items);

        return dto;
    }
}
