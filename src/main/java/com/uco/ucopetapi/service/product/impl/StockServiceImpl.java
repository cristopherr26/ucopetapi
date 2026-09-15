package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.ProductDomain;
import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.dto.product.StockDTO;
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
    private final EntityManager entityManager;

    public StockServiceImpl(StockRepository stockRepository, ProductRepository productRepository,
                            EntityManager entityManager) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
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

        StockDomain stock = stockRepository.findByProduct_IdAndHeadquarter_Id(productId, headquarterId)
                .orElse(null);

        if (stock != null) {
            int newQuantity = stock.getQuantity() + quantity;
            if (newQuantity < 0) {
                throw new IllegalStateException(
                        "La operación dejaría el stock en negativo (actual: " + stock.getQuantity() + ", ajuste: " + quantity + ")");
            }
            stock.setQuantity(newQuantity);
        } else {
            if (quantity < 0) {
                throw new IllegalStateException(
                        "No existe stock de este producto en esta sede; no se puede restar de una cantidad que no existe");
            }
            ProductDomain product = entityManager.getReference(ProductDomain.class, productId);
            HeadquarterDomain headquarter = entityManager.getReference(HeadquarterDomain.class, headquarterId);
            stock = new StockDomain(UUID.randomUUID(), product, headquarter, quantity);
        }

        stock = stockRepository.save(stock);
        return toDto(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public StockDTO findByProductAndHeadquarter(UUID productId, UUID headquarterId) {
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