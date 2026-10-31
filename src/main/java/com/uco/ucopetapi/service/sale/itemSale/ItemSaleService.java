package com.uco.ucopetapi.service.sale.itemSale;

import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDomain;
import com.uco.ucopetapi.domain.sale.sale.AddItemSaleRequestDomain;
import com.uco.ucopetapi.domain.sale.sale.EditItemSaleRequestDomain;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;

import java.util.List;
import java.util.UUID;

public interface ItemSaleService {

    SaleOrderDomain addItemToSaleOrder(
            UUID saleOrderId,
            AddItemSaleRequestDomain request
    );

    SaleOrderDomain editItemInSaleOrder(
            UUID saleOrderId,
            UUID itemSaleId,
            EditItemSaleRequestDomain request
    );

    SaleOrderDomain removeItemFromSaleOrder(
            UUID saleOrderId,
            UUID itemSaleId
    );

    List<ItemSaleDomain> listItemsBySaleOrder(UUID saleOrderId);
}
