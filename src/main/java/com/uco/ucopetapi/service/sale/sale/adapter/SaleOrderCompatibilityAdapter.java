package com.uco.ucopetapi.service.sale.sale.adapter;

import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.sale.SaleOrderDTO;
import com.uco.ucopetapi.service.sale.sale.SaleOrderService;
import org.springframework.stereotype.Component;

@Component
public class SaleOrderCompatibilityAdapter {

    private final SaleOrderService saleOrderService;

    public SaleOrderCompatibilityAdapter(SaleOrderService saleOrderService) {
        this.saleOrderService = saleOrderService;
    }

    public SaleOrderDTO registerNewSaleOrder(SaleOrderDTO request) {

        SaleOrderDomain saleOrderDomain = new SaleOrderDomain(
                request.getId(),
                request.getHeadquarterId(),
                request.getOrderNumber(),
                request.getSaleDate(),
                request.getClientID(),
                request.getPetId(),
                request.getHealthPlanId(),
                request.getSubtotal(),
                request.getTotalDiscount(),
                request.getTotalTaxes(),
                request.getTotal(),
                request.getComment(),
                request.getState()
        );

        SaleOrderDomain registeredSale =
                saleOrderService.registerNewSaleOrder(saleOrderDomain);

        return new SaleOrderDTO(registeredSale.getId());
    }
}
