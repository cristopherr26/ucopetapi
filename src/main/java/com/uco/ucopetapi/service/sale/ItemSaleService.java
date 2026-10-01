package com.uco.ucopetapi.service.sale;

import com.uco.ucopetapi.dto.sale.AddItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.EditItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;

import java.util.List;
import java.util.UUID;

public interface ItemSaleService {

    SaleOrderDTO addItemToSaleOrder(UUID saleOrderId, AddItemSaleRequestDTO request);

    SaleOrderDTO editItemInSaleOrder(UUID saleOrderId, UUID itemSaleId, EditItemSaleRequestDTO request);

    SaleOrderDTO removeItemFromSaleOrder(UUID saleOrderId, UUID itemSaleId);

    List<ItemSaleDTO> listItemsBySaleOrder(UUID saleOrderId);

}
