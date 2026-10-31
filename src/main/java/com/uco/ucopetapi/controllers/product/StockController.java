package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.dto.product.StockMovementDTO;
import com.uco.ucopetapi.service.product.StockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public StockDTO getByProductAndHeadquarter(@RequestParam UUID productId, @RequestParam UUID headquarterId) {
        return stockService.findByProductAndHeadquarter(productId, headquarterId);
    }

    @GetMapping("/{productId}")
    public List<StockDTO> listByProduct(@PathVariable UUID productId) {
        return stockService.findByProduct(productId);
    }

    @GetMapping("/{productId}/movements")
    public List<StockMovementDTO> listMovements(@PathVariable UUID productId,
                                                @RequestParam(required = false) UUID headquarterId) {
        return stockService.findMovements(productId, headquarterId);
    }

    @PostMapping("/adjust")
    public StockDTO adjust(@Valid @RequestBody StockMovementDTO request) {
        return stockService.adjustStock(request.getProductId(), request.getHeadquarterId(), request.getQuantity(),
                request.getReason(), request.getNote());
    }
}
