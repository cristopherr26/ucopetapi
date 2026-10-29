package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.ProductDomain;
import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.domain.product.enums.StockMovementReason;
import com.uco.ucopetapi.dto.product.ProcessorReplyDTO;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.dto.product.StockMovementDTO;
import com.uco.ucopetapi.messaging.product.StockAdjustSenderBroker;
import com.uco.ucopetapi.repository.product.IHeadquarterReferenceRepository;
import com.uco.ucopetapi.repository.product.IProductRepository;
import com.uco.ucopetapi.repository.product.IStockMovementRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class StockService {

    private final IStockRepository stockRepository;
    private final IProductRepository productRepository;
    private final IHeadquarterReferenceRepository headquarterRepository;
    private final StockAdjustSenderBroker stockAdjustSenderBroker;
    private final IStockMovementRepository stockMovementRepository;

    public StockService(IStockRepository stockRepository, IProductRepository productRepository,
                        IHeadquarterReferenceRepository headquarterRepository,
                        StockAdjustSenderBroker stockAdjustSenderBroker,
                        IStockMovementRepository stockMovementRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.headquarterRepository = headquarterRepository;
        this.stockAdjustSenderBroker = stockAdjustSenderBroker;
        this.stockMovementRepository = stockMovementRepository;
    }

    public StockDTO adjustStock(UUID productId, UUID headquarterId, Integer quantity) {
        return adjustStock(productId, headquarterId, quantity, StockMovementReason.ADJUSTMENT, null);
    }

    public StockDTO adjustStock(UUID productId, UUID headquarterId, Integer quantity, StockMovementReason reason,
                                String note) {
        if (quantity == null || quantity == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad a ajustar no puede ser nula ni cero");
        }
        StockMovementReason movementReason = reason != null ? reason : StockMovementReason.ADJUSTMENT;
        requireSignMatchesReason(movementReason, quantity);
        requireActiveProduct(productId);
        requireActiveHeadquarter(headquarterId);
        StockDomain stock = stockRepository.findByProduct_IdAndHeadquarterId(productId, headquarterId).orElse(null);
        requireNotNegative(stock, quantity);
        StockMovementDTO message = new StockMovementDTO();
        message.setProductId(productId);
        message.setHeadquarterId(headquarterId);
        message.setQuantity(quantity);
        message.setReason(movementReason);
        message.setNote(note);
        message.setPerformedBy(currentPerson());
        ProcessorReplyDTO reply = stockAdjustSenderBroker.executeAndWait(message, UUID.randomUUID().toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "El procesador de inventario no respondió a tiempo: el ajuste no se aplicó"));
        if (!reply.applied()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reply.reason());
        }
        return new StockDTO(reply.stockId(), productId, headquarterId, reply.quantity());
    }

    @Transactional(readOnly = true)
    public StockDTO findByProductAndHeadquarter(UUID productId, UUID headquarterId) {
        requireProduct(productId);
        requireHeadquarter(headquarterId);
        return stockRepository.findByProduct_IdAndHeadquarterId(productId, headquarterId)
                .map(StockService::toDTO)
                .orElseGet(() -> new StockDTO(null, productId, headquarterId, 0));
    }

    @Transactional(readOnly = true)
    public List<StockDTO> findByProduct(UUID productId) {
        requireProduct(productId);
        return stockRepository.findByProduct_Id(productId).stream()
                .map(StockService::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementDTO> findMovements(UUID productId, UUID headquarterId) {
        requireProduct(productId);
        if (headquarterId == null) {
            return stockMovementRepository.findByProduct_IdOrderByOccurredAtDesc(productId).stream()
                    .map(StockMovementDTO::of)
                    .toList();
        }
        requireHeadquarter(headquarterId);
        return stockMovementRepository.findByProduct_IdAndHeadquarterIdOrderByOccurredAtDesc(productId, headquarterId)
                .stream()
                .map(StockMovementDTO::of)
                .toList();
    }

    private static void requireSignMatchesReason(StockMovementReason reason, int quantity) {
        int requiredSign = switch (reason) {
            case OPENING -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El saldo inicial lo registra el sistema; no se puede enviar como motivo");
            case PURCHASE, RETURN, TRANSFER_IN -> 1;
            case SALE, TRANSFER_OUT, DISPENSE, LOSS -> -1;
            case ADJUSTMENT, COUNT -> 0;
        };
        if (requiredSign > 0 && quantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un movimiento " + reason + " solo puede sumar stock");
        }
        if (requiredSign < 0 && quantity > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un movimiento " + reason + " solo puede restar stock");
        }
    }

    private static UUID currentPerson() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    private static void requireNotNegative(StockDomain current, int quantity) {
        if (current == null && quantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No existe stock de este producto en esta sede; no se puede restar de una cantidad que no existe");
        }
        int result = (current != null ? current.getQuantity() : 0) + quantity;
        if (result < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La operación dejaría el stock en negativo");
        }
    }

    private void requireActiveProduct(UUID productId) {
        ProductDomain product = productId == null ? null : productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new NoSuchElementException("Producto no encontrado: " + productId);
        }
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede ajustar el stock de un producto dado de baja");
        }
    }

    private void requireActiveHeadquarter(UUID headquarterId) {
        requireHeadquarter(headquarterId);
        if (!headquarterRepository.existsByIdAndActiveTrue(headquarterId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La sede " + headquarterId + " está dada de baja");
        }
    }

    private void requireProduct(UUID productId) {
        if (productId == null || !productRepository.existsById(productId)) {
            throw new NoSuchElementException("Producto no encontrado: " + productId);
        }
    }

    private void requireHeadquarter(UUID headquarterId) {
        if (headquarterId == null || !headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
    }

    private static StockDTO toDTO(StockDomain stock) {
        return new StockDTO(stock.getId(), stock.getProduct().getId(), stock.getHeadquarterId(), stock.getQuantity());
    }
}
