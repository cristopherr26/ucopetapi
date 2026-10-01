package com.uco.ucopetapi.service.purchases;

import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseItem;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.domain.purchases.TaxCategory;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.purchases.LinkExpenseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseIdResponseDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseItemRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.Item;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.repository.purchases.PurchaseItemRepository;
import com.uco.ucopetapi.repository.purchases.PurchaseRepository;
import com.uco.ucopetapi.service.person.PersonService;
import com.uco.ucopetapi.service.purchases.exception.DuplicatePurchaseNumberException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNumberNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final PersonService personService;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                                PurchaseItemRepository purchaseItemRepository,
                                PersonService personService) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.personService = personService;
    }

    @Override
    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO request) {
        if (purchaseRepository.existsByPurchaseNumber(request.purchaseNumber())) {
            throw new DuplicatePurchaseNumberException(request.purchaseNumber());
        }

        BigDecimal totalTaxes = BigDecimal.ZERO;
        for (PurchaseItemRequestDTO itemRequest : request.items()) {
            BigDecimal itemSubtotal = itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            BigDecimal itemTax = itemSubtotal.multiply(taxRateFor(itemRequest.taxCategory()))
                    .setScale(2, RoundingMode.HALF_UP);
            totalTaxes = totalTaxes.add(itemTax);
        }

        Purchase purchase = toEntity(request, totalTaxes);
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO getPurchaseById(UUID id) {
        return toResponseDTO(findPurchaseOrThrow(id));
    }

    @Override
    public Page<PurchaseResponseDTO> listPurchases(UUID headquarterId, PurchaseStatus status, UUID supplierId, Pageable pageable) {
        Page<Purchase> purchases;
        if (status != null && supplierId != null) {
            purchases = purchaseRepository.findByHeadquarterIdAndStatusAndSupplierId(headquarterId, status, supplierId, pageable);
        } else if (status != null) {
            purchases = purchaseRepository.findByHeadquarterIdAndStatus(headquarterId, status, pageable);
        } else if (supplierId != null) {
            purchases = purchaseRepository.findByHeadquarterIdAndSupplierId(headquarterId, supplierId, pageable);
        } else {
            purchases = purchaseRepository.findByHeadquarterId(headquarterId, pageable);
        }
        return purchases.map(this::toResponseDTO);
    }

    @Override
    public List<Item> getPurchaseItems(UUID id) {
        findPurchaseOrThrow(id);
        return purchaseItemRepository.findByPurchaseId(id).stream()
                .map(this::toItemDTO)
                .toList();
    }

    @Override
    public PurchaseResponseDTO receivePurchase(UUID id) {
        Purchase purchase = findPurchaseOrThrow(id);
        purchase.setStatus(PurchaseStatus.RECEIVED);
        purchase.setUpdatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO cancelPurchase(UUID id) {
        Purchase purchase = findPurchaseOrThrow(id);
        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase.setUpdatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO linkExpense(UUID id, LinkExpenseRequestDTO request) {
        Purchase purchase = findPurchaseOrThrow(id);
        purchase.setExpenseId(request.expenseId());
        purchase.setStatus(PurchaseStatus.LINKED);
        purchase.setUpdatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseIdResponseDTO findIdByPurchaseNumber(String purchaseNumber) {
        Purchase purchase = purchaseRepository.findByPurchaseNumber(purchaseNumber)
                .orElseThrow(() -> new PurchaseNumberNotFoundException(purchaseNumber));
        return new PurchaseIdResponseDTO(purchase.getId());
    }

    private Purchase findPurchaseOrThrow(UUID id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
    }

    private BigDecimal taxRateFor(TaxCategory category) {
        return switch (category) {
            case STANDARD -> new BigDecimal("0.19");
            case REDUCED -> new BigDecimal("0.05");
            case EXEMPT -> BigDecimal.ZERO;
        };
    }

    private RelatedEntityDTO resolvePerson(UUID personId) {
        try {
            PersonDTO person = personService.findById(personId);
            return new RelatedEntityDTO(personId, person.firstName() + " " + person.lastName());
        } catch (ResponseStatusException e) {
            return new RelatedEntityDTO(personId, null);
        }
    }

    private UUID currentPersonId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private Purchase toEntity(PurchaseRequestDTO request, BigDecimal totalTaxes) {
        List<PurchaseItem> items = request.items().stream()
                .map(this::toItemEntity)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UUID currentPersonId = currentPersonId();

        return Purchase.builder()
                .supplierId(request.supplierId())
                .purchaseNumber(request.purchaseNumber())
                .headquarterId(request.headquarterId())
                .hasDiscount(request.hasDiscount() == null ? false : request.hasDiscount())
                .purchaseDate(LocalDateTime.now(ZoneId.of("America/Bogota")))
                .status(PurchaseStatus.PENDING)
                .createdAt(LocalDateTime.now(ZoneId.of("America/Bogota")))
                .updatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")))
                .createdByPersonId(currentPersonId)
                .updatedByPersonId(currentPersonId)
                .subtotal(subtotal)
                .totalTaxes(totalTaxes)
                .total(subtotal.add(totalTaxes))
                .items(items)
                .build();
    }

    private PurchaseItem toItemEntity(PurchaseItemRequestDTO request) {
        PurchaseItem item = new PurchaseItem();
        item.setProductId(request.productId());
        item.setItemType(request.itemType());
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        item.setSubtotal(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())));
        item.setTaxCategory(request.taxCategory());
        return item;
    }

    private PurchaseResponseDTO toResponseDTO(Purchase purchase) {
        List<Item> items = purchase.getItems() == null
                ? List.of()
                : purchase.getItems().stream()
                        .map(this::toItemDTO)
                        .toList();

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
                new RelatedEntityDTO(purchase.getHeadquarterId(), null),
                purchase.isHasDiscount(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt(),
                resolvePerson(purchase.getCreatedByPersonId()),
                resolvePerson(purchase.getUpdatedByPersonId())
        );
    }

    private Item toItemDTO(PurchaseItem item) {
        return new Item(
                new RelatedEntityDTO(item.getProductId(), null),
                item.getItemType(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
