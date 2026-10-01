package com.uco.ucopetapi.controllers.sale.impl;

import com.uco.ucopetapi.controllers.sale.ItemSaleController;
import com.uco.ucopetapi.dto.sale.AddItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.EditItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.service.sale.ItemSaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<ItemSaleDTO>> listItems(@PathVariable UUID saleOrderId) {
        return ResponseEntity.ok(itemSaleService.listItemsBySaleOrder(saleOrderId));
    }

    @Override
    @PostMapping
    public ResponseEntity<SaleOrderDTO> addItem(@PathVariable UUID saleOrderId, @RequestBody AddItemSaleRequestDTO request) {
        return ResponseEntity.ok(itemSaleService.addItemToSaleOrder(saleOrderId, request));
    }

    @Override
    @PutMapping("/{itemSaleId}")
    public ResponseEntity<SaleOrderDTO> editItem(@PathVariable UUID saleOrderId, @PathVariable UUID itemSaleId, @RequestBody EditItemSaleRequestDTO request) {
        return ResponseEntity.ok(itemSaleService.editItemInSaleOrder(saleOrderId, itemSaleId, request));
    }

    @Override
    @DeleteMapping("/{itemSaleId}")
    public ResponseEntity<SaleOrderDTO> removeItem(@PathVariable UUID saleOrderId, @PathVariable UUID itemSaleId) {
        return ResponseEntity.ok(itemSaleService.removeItemFromSaleOrder(saleOrderId, itemSaleId));
    }

}
