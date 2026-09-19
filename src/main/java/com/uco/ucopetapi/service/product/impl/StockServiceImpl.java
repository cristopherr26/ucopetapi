package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.ProductDomain;
import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.ProductRepository;
import com.uco.ucopetapi.repository.product.StockRepository;
import com.uco.ucopetapi.service.product.StockService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final HeadquarterRepository headquarterRepository;
    private final EntityManager entityManager;

    public StockServiceImpl(StockRepository stockRepository, ProductRepository productRepository,
                            HeadquarterRepository headquarterRepository, EntityManager entityManager) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.headquarterRepository = headquarterRepository;
        this.entityManager = entityManager;
    }

    @Override
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

        int updatedRows = stockRepository.applyAdjustment(productId, headquarterId, quantity);

        if (updatedRows == 0) {
            handleZeroRowsUpdated(productId, headquarterId, quantity);
        }

        StockDomain stock = stockRepository.findByProduct_IdAndHeadquarter_Id(productId, headquarterId)
                .orElseThrow(() -> new NoSuchElementException("Stock no encontrado tras el ajuste"));
        return toDto(stock);
    }

    private void handleZeroRowsUpdated(UUID productId, UUID headquarterId, Integer quantity) {
        boolean existingRow = stockRepository.findByProduct_IdAndHeadquarter_Id(productId, headquarterId).isPresent();
        if (existingRow) {
            throw new IllegalStateException("La operación dejaría el stock en negativo");
        }
        if (quantity < 0) {
            throw new IllegalStateException(
                    "No existe stock de este producto en esta sede; no se puede restar de una cantidad que no existe");
        }
        HeadquarterDomain headquarter = headquarterRepository.findById(headquarterId)
                .orElseThrow(() -> new NoSuchElementException("Sede no encontrada: " + headquarterId));
        ProductDomain product = entityManager.getReference(ProductDomain.class, productId);
        stockRepository.save(new StockDomain(UUID.randomUUID(), product, headquarter, quantity));
    }

    @Override
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

    @Override
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