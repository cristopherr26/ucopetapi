package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.dto.product.AdjustStockRequestDTO;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.service.product.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
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
        return ResponseEntity.ok(stockService.adjustStock(request.getProductId(), request.getHeadquarterId(), request.getQuantity()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleInvalidData(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}