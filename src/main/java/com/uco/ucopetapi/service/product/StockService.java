package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.messaging.inventory.InventoryCommand;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.ICatalogItemRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class StockService {

    private final IStockRepository stockRepository;
    private final ICatalogItemRepository itemRepository;
    private final HeadquarterRepository headquarterRepository;
    private final MessageSender<InventoryCommand> messageSender;

    public StockService(IStockRepository stockRepository, ICatalogItemRepository itemRepository,
                            HeadquarterRepository headquarterRepository,
                            MessageSender<InventoryCommand> messageSender) {
        this.stockRepository = stockRepository;
        this.itemRepository = itemRepository;
        this.headquarterRepository = headquarterRepository;
        this.messageSender = messageSender;
    }

    @Transactional
    public StockDTO adjustStock(UUID productId, UUID headquarterId, Integer quantity) {
        validateAdjust(productId, headquarterId, quantity);
        if (stockRepository.addQuantity(productId, headquarterId, quantity) == 0) {
            createOrReject(productId, headquarterId, quantity);
        }
        return toDto(stockRepository.findByItem_IdAndHeadquarter_Id(productId, headquarterId).orElseThrow());
    }

    @Transactional(readOnly = true)
    public void prepareAdjust(UUID productId, UUID headquarterId, Integer quantity) {
        validateAdjust(productId, headquarterId, quantity);
        if (quantity > 0) {
            return;
        }
        StockDomain current = stockRepository.findByItem_IdAndHeadquarter_Id(productId, headquarterId)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe stock de este producto en esta sede; no se puede restar de una cantidad que no existe"));
        if (current.getQuantity() + quantity < 0) {
            throw new IllegalStateException(
                    "La operación dejaría el stock en negativo (actual: " + current.getQuantity() + ", ajuste: " + quantity + ")");
        }
    }

    public void submitAdjust(UUID productId, UUID headquarterId, Integer quantity) {
        prepareAdjust(productId, headquarterId, quantity);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("productId", productId);
        data.put("headquarterId", headquarterId);
        data.put("quantity", quantity);
        messageSender.execute(new InventoryCommand("stock-adjust", data), UUID.randomUUID().toString());
    }

    private void validateAdjust(UUID productId, UUID headquarterId, Integer quantity) {
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("La cantidad a ajustar no puede ser nula ni cero");
        }
        if (itemRepository.findByIdAndType(productId, CatalogItemType.PRODUCT).isEmpty()) {
            throw new NoSuchElementException("Producto no encontrado: " + productId);
        }
        if (!headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
    }

    private void createOrReject(UUID productId, UUID headquarterId, int quantity) {
        if (quantity > 0) {
            stockRepository.createOrAddQuantity(UUID.randomUUID(), productId, headquarterId, quantity);
            return;
        }
        StockDomain current = stockRepository.findByItem_IdAndHeadquarter_Id(productId, headquarterId)
                .orElse(null);
        if (current == null) {
            throw new IllegalStateException(
                    "No existe stock de este producto en esta sede; no se puede restar de una cantidad que no existe");
        }
        throw new IllegalStateException(
                "La operación dejaría el stock en negativo (actual: " + current.getQuantity() + ", ajuste: " + quantity + ")");
    }

    @Transactional(readOnly = true)
    public StockDTO findByProductAndHeadquarter(UUID productId, UUID headquarterId) {
        if (!itemRepository.existsById(productId)) {
            throw new NoSuchElementException("Producto no encontrado: " + productId);
        }
        if (!headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
        return stockRepository.findByItem_IdAndHeadquarter_Id(productId, headquarterId)
                .map(this::toDto)
                .orElseGet(() -> new StockDTO(null, productId, headquarterId, 0));
    }

    @Transactional(readOnly = true)
    public List<StockDTO> findByProduct(UUID productId) {
        return stockRepository.findByItem_Id(productId).stream()
                .map(this::toDto)
                .toList();
    }

    private StockDTO toDto(StockDomain stock) {
        return new StockDTO(stock.getId(), stock.getItem().getId(), stock.getHeadquarter().getId(), stock.getQuantity());
    }
}