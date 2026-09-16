package com.uco.ucopetapi.service.purchases;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseItem;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.dto.purchases.LinkExpenseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseItemRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.Item;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.repository.purchases.PurchaseItemRepository;
import com.uco.ucopetapi.repository.purchases.PurchaseRepository;
import com.uco.ucopetapi.service.headquarter.HeadquarterService;
import com.uco.ucopetapi.service.person.PersonService;
import com.uco.ucopetapi.service.provider.ProviderService;
import com.uco.ucopetapi.service.purchases.exception.HeadquarterInactiveException;
import com.uco.ucopetapi.service.purchases.exception.HeadquarterNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.SupplierNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProviderService providerService;
    private final HeadquarterService headquarterService;
    private final PersonService personService;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                                PurchaseItemRepository purchaseItemRepository,
                                ProviderService providerService,
                                HeadquarterService headquarterService,
                                PersonService personService) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.providerService = providerService;
        this.headquarterService = headquarterService;
        this.personService = personService;
    }

    @Override
    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO request) {
        resolveSupplier(request.supplierId());
        HeadquarterDomain headquarter = findHeadquarterOrThrow(request.headquarterId());
        if (!Boolean.TRUE.equals(headquarter.getIsActive())) {
            throw new HeadquarterInactiveException(headquarter.getName());
        }

        Purchase purchase = toEntity(request);
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO getPurchaseById(UUID id) {
        return toResponseDTO(findPurchaseOrThrow(id));
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
        findPurchaseOrThrow(id);
        return purchaseItemRepository.findByPurchaseId(id).stream()
                .map(this::toItemDTO)
                .toList();
    }

    @Override
    public PurchaseResponseDTO receivePurchase(UUID id) {
        Purchase purchase = findPurchaseOrThrow(id);
        purchase.setStatus(PurchaseStatus.RECEIVED);
        purchase.setUpdatedAt(LocalDateTime.now());
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO cancelPurchase(UUID id) {
        Purchase purchase = findPurchaseOrThrow(id);
        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase.setUpdatedAt(LocalDateTime.now());
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO linkExpense(UUID id, LinkExpenseRequestDTO request) {
        Purchase purchase = findPurchaseOrThrow(id);
        purchase.setExpenseId(request.expenseId());
        purchase.setStatus(PurchaseStatus.LINKED);
        purchase.setUpdatedAt(LocalDateTime.now());
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    private Purchase findPurchaseOrThrow(UUID id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
    }

    private HeadquarterDomain findHeadquarterOrThrow(UUID headquarterId) {
        return headquarterService.findById(headquarterId)
                .orElseThrow(() -> new HeadquarterNotFoundException(headquarterId));
    }

    private RelatedEntityDTO resolveSupplier(UUID supplierId) {
        try {
            ProviderDTO provider = providerService.findById(supplierId);
            return new RelatedEntityDTO(supplierId, provider.getProviderName());
        } catch (NoSuchElementException e) {
            throw new SupplierNotFoundException(supplierId);
        }
    }

    private RelatedEntityDTO resolveHeadquarter(UUID headquarterId) {
        HeadquarterDomain headquarter = findHeadquarterOrThrow(headquarterId);
        return new RelatedEntityDTO(headquarterId, headquarter.getName());
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
        UUID currentPersonId = currentPersonId();
        purchase.setCreatedByPersonId(currentPersonId);
        purchase.setUpdatedByPersonId(currentPersonId);

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
                resolveSupplier(purchase.getSupplierId()),
                items,
                purchase.getPurchaseDate(),
                purchase.getSubtotal(),
                purchase.getTotalTaxes(),
                purchase.getTotal(),
                purchase.getStatus(),
                purchase.getExpenseId(),
                resolveHeadquarter(purchase.getHeadquarterId()),
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
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
