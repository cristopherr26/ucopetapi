package com.uco.ucopetapi.service.purchases;

import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseItem;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.purchases.LinkExpenseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseItemRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.Item;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.repository.purchases.PurchaseItemRepository;
import com.uco.ucopetapi.repository.purchases.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository, PurchaseItemRepository purchaseItemRepository) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
    }

    @Override
    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO request) {
        Purchase purchase = toEntity(request);
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO getPurchaseById(UUID id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        return toResponseDTO(purchase);
    }

    @Override
    public List<PurchaseResponseDTO> listPurchases(UUID headquarterId, PurchaseStatus status, UUID supplierId) {
        List<Purchase> purchases;
        if (status != null && supplierId != null) {
            purchases = purchaseRepository.findByHeadquarterIdAndStatusAndSupplierId(headquarterId, status, supplierId);
        } else if (status != null) {
            purchases = purchaseRepository.findByHeadquarterIdAndStatus(headquarterId, status);
        } else if (supplierId != null) {
            purchases = purchaseRepository.findByHeadquarterIdAndSupplierId(headquarterId, supplierId);
        } else {
            purchases = purchaseRepository.findByHeadquarterId(headquarterId);
        }
        return purchases.stream().map(this::toResponseDTO).toList();
    }

    @Override
    public List<Item> getPurchaseItems(UUID id) {
        return purchaseItemRepository.findByPurchaseId(id).stream()
                .map(this::toItemDTO)
                .toList();
    }

    @Override
    public PurchaseResponseDTO receivePurchase(UUID id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        purchase.setStatus(PurchaseStatus.RECEIVED);
        purchase.setUpdatedAt(LocalDateTime.now());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO cancelPurchase(UUID id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase.setUpdatedAt(LocalDateTime.now());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO linkExpense(UUID id, LinkExpenseRequestDTO request) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
        purchase.setExpenseId(request.expenseId());
        purchase.setStatus(PurchaseStatus.LINKED);
        purchase.setUpdatedAt(LocalDateTime.now());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    private Purchase toEntity(PurchaseRequestDTO request) {
        Purchase purchase = new Purchase();
        purchase.setSupplierId(request.supplierId());
        purchase.setPurchaseNumber(request.purchaseNumber());
        purchase.setHeadquarterId(request.headquarterId());
        purchase.setHasDiscount(request.hasDiscount() == null ? false : request.hasDiscount());
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setStatus(PurchaseStatus.PENDING);
        purchase.setCreatedAt(LocalDateTime.now());
        purchase.setUpdatedAt(LocalDateTime.now());

        List<PurchaseItem> items = request.items().stream()
                .map(itemRequest -> toItemEntity(itemRequest, purchase))
                .toList();
        items.forEach(purchase::addItem);

        BigDecimal subtotal = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTaxes = BigDecimal.ZERO;

        purchase.setSubtotal(subtotal);
        purchase.setTotalTaxes(totalTaxes);
        purchase.setTotal(subtotal.add(totalTaxes));

        return purchase;
    }

    private PurchaseItem toItemEntity(PurchaseItemRequestDTO request, Purchase purchase) {
        PurchaseItem item = new PurchaseItem();
        item.setPurchase(purchase);
        item.setProductId(request.productId());
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        item.setSubtotal(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())));
        return item;
    }

    private PurchaseResponseDTO toResponseDTO(Purchase purchase) {
        List<Item> items = purchase.getItems() == null
                ? List.of()
                : purchase.getItems().stream().map(this::toItemDTO).toList();

        return new PurchaseResponseDTO(
                purchase.getId(),
                purchase.getPurchaseNumber(),
                new RelatedEntityDTO(purchase.getSupplierId(), null),
                items,
                purchase.getPurchaseDate(),
                purchase.getSubtotal(),
                purchase.getTotalTaxes(),
                purchase.getTotal(),
                purchase.getStatus(),
                purchase.getExpenseId(),
                purchase.getHeadquarterId(),
                purchase.isHasDiscount(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }

    private Item toItemDTO(PurchaseItem item) {
        return new Item(
                new RelatedEntityDTO(item.getProductId(), null),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
