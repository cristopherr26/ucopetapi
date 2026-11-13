package com.uco.ucopetapi.controllers.sale.itemSale;

import com.uco.ucopetapi.domain.sale.sale.AddItemSaleRequestDomain;
import com.uco.ucopetapi.domain.sale.sale.EditItemSaleRequestDomain;
import com.uco.ucopetapi.dto.sale.itemSale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.sale.SaleOrderDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface ItemSaleController {

    ResponseEntity<SaleOrderDTO> addItem(UUID saleOrderId, AddItemSaleRequestDomain request);

    ResponseEntity<SaleOrderDTO> editItem(UUID saleOrderId, UUID itemSaleId, EditItemSaleRequestDomain request);

    ResponseEntity<SaleOrderDTO> removeItem(UUID saleOrderId, UUID itemSaleId);

    ResponseEntity<List<ItemSaleDTO>> listItems(UUID saleOrderId);

}
