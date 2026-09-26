package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.IProductRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class StockService {

    private final IStockRepository stockRepository;
    private final IProductRepository productRepository;
    private final HeadquarterRepository headquarterRepository;

    public StockService(IStockRepository stockRepository, IProductRepository productRepository,
                            HeadquarterRepository headquarterRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.headquarterRepository = headquarterRepository;
    }

    @Transactional
    public StockDTO adjustStock(UUID productId, UUID headquarterId, Integer quantity) {
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("La cantidad a ajustar no puede ser nula ni cero");
        }
        if (!productRepository.existsById(productId)) {
            throw new NoSuchElementException("Producto no encontrado: " + productId);
        }
        if (!headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
        if (stockRepository.addQuantity(productId, headquarterId, quantity) == 0) {
            createOrReject(productId, headquarterId, quantity);
        }
        return toDto(stockRepository.findByProduct_IdAndHeadquarter_Id(productId, headquarterId).orElseThrow());
    }

    private void createOrReject(UUID productId, UUID headquarterId, int quantity) {
        StockDomain current = stockRepository.findByProduct_IdAndHeadquarter_Id(productId, headquarterId)
                .orElse(null);
        if (current != null) {
            throw new IllegalStateException(
                    "La operación dejaría el stock en negativo (actual: " + current.getQuantity() + ", ajuste: " + quantity + ")");
        }
        if (quantity < 0) {
            throw new IllegalStateException(
                    "No existe stock de este producto en esta sede; no se puede restar de una cantidad que no existe");
        }
        stockRepository.createOrAddQuantity(UUID.randomUUID(), productId, headquarterId, quantity);
    }

    @Transactional(readOnly = true)
    public StockDTO findByProductAndHeadquarter(UUID productId, UUID headquarterId) {
        if (!productRepository.existsById(productId)) {
            throw new NoSuchElementException("Producto no encontrado: " + productId);
        }
        if (!headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
        return stockRepository.findByProduct_IdAndHeadquarter_Id(productId, headquarterId)
                .map(this::toDto)
                .orElseGet(() -> new StockDTO(null, productId, headquarterId, 0));
    }

    @Transactional(readOnly = true)
    public List<StockDTO> findByProduct(UUID productId) {
        return stockRepository.findByProduct_Id(productId).stream()
                .map(this::toDto)
                .toList();
    }

    private StockDTO toDto(StockDomain stock) {
        return new StockDTO(stock.getId(), stock.getProduct().getId(), stock.getHeadquarter().getId(), stock.getQuantity());
    }
}