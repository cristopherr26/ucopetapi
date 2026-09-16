package com.uco.ucopetapi.service.purchases;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseItem;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
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
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.provider.ProviderService;
import com.uco.ucopetapi.service.purchases.exception.DuplicatePurchaseNumberException;
import com.uco.ucopetapi.service.purchases.exception.HeadquarterInactiveException;
import com.uco.ucopetapi.service.purchases.exception.HeadquarterNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.ProductInactiveException;
import com.uco.ucopetapi.service.purchases.exception.ProductNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.SupplierNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ProductService productService;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                                PurchaseItemRepository purchaseItemRepository,
                                ProviderService providerService,
                                HeadquarterService headquarterService,
                                PersonService personService,
                                ProductService productService) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.providerService = providerService;
        this.headquarterService = headquarterService;
        this.personService = personService;
        this.productService = productService;
    }

    @Override
    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO request) {
        if (purchaseRepository.existsByPurchaseNumber(request.purchaseNumber())) {
            throw new DuplicatePurchaseNumberException(request.purchaseNumber());
        }

        resolveSupplier(request.supplierId());
        HeadquarterDomain headquarter = findHeadquarterOrThrow(request.headquarterId());
        if (!Boolean.TRUE.equals(headquarter.getIsActive())) {
            throw new HeadquarterInactiveException(headquarter.getName());
        }

        for (PurchaseItemRequestDTO itemRequest : request.items()) {
            validateProduct(itemRequest.productId(), request.headquarterId());
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
        Purchase purchase = findPurchaseOrThrow(id);
        return purchaseItemRepository.findByPurchaseId(id).stream()
                .map(item -> toItemDTO(item, purchase.getHeadquarterId()))
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

    private void validateProduct(UUID productId, UUID headquarterId) {
        ProductDTO product;
        try {
            product = productService.getById(productId, headquarterId);
        } catch (NoSuchElementException e) {
            throw new ProductNotFoundException(productId);
        }
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductInactiveException(product.getName());
        }
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

    private RelatedEntityDTO resolveProduct(UUID productId, UUID headquarterId) {
        try {
            ProductDTO product = productService.getById(productId, headquarterId);
            return new RelatedEntityDTO(productId, product.getName());
        } catch (NoSuchElementException e) {
            return new RelatedEntityDTO(productId, null);
        }
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
        List<PurchaseItem> items = request.items().stream()
                .map(this::toItemEntity)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTaxes = BigDecimal.ZERO;

        UUID currentPersonId = currentPersonId();

        return Purchase.builder()
                .supplierId(request.supplierId())
                .purchaseNumber(request.purchaseNumber())
                .headquarterId(request.headquarterId())
                .hasDiscount(request.hasDiscount() == null ? false : request.hasDiscount())
                .purchaseDate(LocalDateTime.now())
                .status(PurchaseStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        item.setSubtotal(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())));
        return item;
    }

    private PurchaseResponseDTO toResponseDTO(Purchase purchase) {
        List<Item> items = purchase.getItems() == null
                ? List.of()
                : purchase.getItems().stream()
                        .map(item -> toItemDTO(item, purchase.getHeadquarterId()))
                        .toList();

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

    private Item toItemDTO(PurchaseItem item, UUID headquarterId) {
        return new Item(
                resolveProduct(item.getProductId(), headquarterId),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
