package com.uco.ucopetapi.controllers.sale;

import com.uco.ucopetapi.dto.sale.AddItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.EditItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface ItemSaleController {

    ResponseEntity<SaleOrderDTO> addItem(UUID saleOrderId, AddItemSaleRequestDTO request);

    ResponseEntity<SaleOrderDTO> editItem(UUID saleOrderId, UUID itemSaleId, EditItemSaleRequestDTO request);

    ResponseEntity<SaleOrderDTO> removeItem(UUID saleOrderId, UUID itemSaleId);

    ResponseEntity<List<ItemSaleDTO>> listItems(UUID saleOrderId);

}
