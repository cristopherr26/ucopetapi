package com.uco.ucopetapi.controllers.sale.impl;

import com.uco.ucopetapi.controllers.sale.SaleOrderController;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import com.uco.ucopetapi.service.sale.SaleOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping
    public ResponseEntity<SaleOrderDTO> registerNewSaleOrder(@RequestBody SaleOrderDTO saleOrderDTO) {
        SaleOrderDTO created = saleOrderService.registerNewSaleOrder(saleOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<SaleOrderDTO> editExistentSaleOrder(@PathVariable UUID id,
                                                              @RequestBody SaleOrderDTO saleOrderDTO) {
        return ResponseEntity.ok(saleOrderService.editExistentSaleOrder(id, saleOrderDTO));
    }

    @Override
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SaleOrderDTO> cancelSaleOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(saleOrderService.cancelSaleOrder(id));
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        SaleOrderDTO filter = new SaleOrderDTO(
                null,
                headquarterId,
                orderNumber,
                null,
                clientID,
                petId,
                healthPlanId,
                0,
                0,
                0,
                0,
                null,
                state
        );

        return ResponseEntity.ok(saleOrderService.findSaleOrderByFilter(filter, dateFrom, dateTo));
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<List<SaleOrderDTO>> findAll() {
        return ResponseEntity.ok(saleOrderService.findAll());
    }
}

