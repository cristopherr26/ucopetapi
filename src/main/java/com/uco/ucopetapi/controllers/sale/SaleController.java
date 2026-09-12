package com.uco.ucopetapi.controllers.sale;

import com.uco.ucopetapi.domain.sale.enums.PaymentMethod;
import com.uco.ucopetapi.domain.sale.enums.SaleOrderState;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales")
public class SaleController {

    @GetMapping("/{id}")
    public SaleOrderDTO findSaleById(@PathVariable String id){return new SaleOrderDTO("OV-001", java.time.LocalDateTime.now(), 100, 10, 10, 100, PaymentMethod.TARJETA, "Comment", SaleOrderState.BORRADOR);}

    @PostMapping
    public SaleOrderDTO registerSale(@RequestBody SaleOrderDTO saleOrderDTO){return saleOrderDTO;}

}
