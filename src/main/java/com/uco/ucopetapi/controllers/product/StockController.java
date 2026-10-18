package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.dto.product.AdjustStockRequestDTO;
import com.uco.ucopetapi.config.InventoryQueueConfig;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.service.product.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;
    private final InventoryQueueConfig queueConfig;

    public StockController(StockService stockService, InventoryQueueConfig queueConfig) {
        this.stockService = stockService;
        this.queueConfig = queueConfig;
    }

    @GetMapping
    public ResponseEntity<StockDTO> getByProductAndHeadquarter(
            @RequestParam UUID productId,
            @RequestParam UUID headquarterId
    ) {
        return ResponseEntity.ok(stockService.findByProductAndHeadquarter(productId, headquarterId));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<StockDTO>> listByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(stockService.findByProduct(productId));
    }

    @PostMapping("/adjust")
    public ResponseEntity<StockDTO> adjust(@RequestBody AdjustStockRequestDTO request) {
        if (queueConfig.isAsyncWrites()) {
            stockService.submitAdjust(request.getProductId(), request.getHeadquarterId(), request.getQuantity());
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.ok(stockService.adjustStock(request.getProductId(), request.getHeadquarterId(), request.getQuantity()));
    }
}