package com.uco.ucopetapi.service.purchases;

import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseItem;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.purchases.LinkExpenseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseItemRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.Item;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.messaging.purchases.PurchaseCreationSenderBroker;
import com.uco.ucopetapi.messaging.purchases.PurchaseDeactivationSenderBroker;
import com.uco.ucopetapi.repository.purchases.PurchaseItemRepository;
import com.uco.ucopetapi.repository.purchases.PurchaseRepository;
import com.uco.ucopetapi.service.person.PersonService;
import com.uco.ucopetapi.service.purchases.exception.DuplicatePurchaseNumberException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNumberNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseStateConflictException;
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
    private final PurchaseCreationSenderBroker purchaseCreationSenderBroker;
    private final PurchaseDeactivationSenderBroker purchaseDeactivationSenderBroker;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                                PurchaseItemRepository purchaseItemRepository,
                                PersonService personService,
                                PurchaseCreationSenderBroker purchaseCreationSenderBroker,
                                PurchaseDeactivationSenderBroker purchaseDeactivationSenderBroker) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.personService = personService;
        this.purchaseCreationSenderBroker = purchaseCreationSenderBroker;
        this.purchaseDeactivationSenderBroker = purchaseDeactivationSenderBroker;
    }

    @Override
    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO request) {
        if (purchaseRepository.existsByPurchaseNumber(request.purchaseNumber())) {
            throw new DuplicatePurchaseNumberException(request.purchaseNumber());
        }

        BigDecimal totalTaxes = BigDecimal.ZERO;
        for (PurchaseItemRequestDTO itemRequest : request.items()) {
            BigDecimal itemSubtotal = itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            BigDecimal itemTax = itemSubtotal.multiply(itemRequest.taxCategory().getRate())
                    .setScale(2, RoundingMode.HALF_UP);
            totalTaxes = totalTaxes.add(itemTax);
        }

        Purchase purchase = toEntity(request, totalTaxes);
        // El id se genera aqui mismo (no en la base de datos): no se llama a
        // purchaseRepository.save(). Quien realmente persiste la compra es el
        // listener de ucopet-commercial-processor al consumir este mensaje,
        // igual que el patron ya usado en healthPlan/pet.
        purchase.setId(UUID.randomUUID());

        String idMessage = UUID.randomUUID().toString();
        purchaseCreationSenderBroker.execute(purchase, idMessage);

        return toResponseDTO(purchase);
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
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new PurchaseStateConflictException(
                    "Solo se puede recibir una compra en estado PENDING. Estado actual: " + purchase.getStatus());
        }
        purchase.setStatus(PurchaseStatus.RECEIVED);
        purchase.setUpdatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public PurchaseResponseDTO cancelPurchase(UUID id) {
        Purchase purchase = findPurchaseOrThrow(id);
        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new PurchaseStateConflictException(
                    "La compra " + purchase.getPurchaseNumber() + " ya se encuentra CANCELLED.");
        }
        if (purchase.getStatus() == PurchaseStatus.LINKED) {
            throw new PurchaseStateConflictException(
                    "No se puede cancelar la compra " + purchase.getPurchaseNumber()
                            + " porque ya tiene un egreso vinculado (LINKED).");
        }
        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchase.setUpdatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        purchase.setUpdatedByPersonId(currentPersonId());

        // Igual que en create: no se llama a purchaseRepository.save(), el
        // listener de ucopet-commercial-processor es quien persiste el cambio.
        String idMessage = UUID.randomUUID().toString();
        purchaseDeactivationSenderBroker.execute(purchase, idMessage);

        return toResponseDTO(purchase);
    }

    @Override
    public PurchaseResponseDTO linkExpense(UUID id, LinkExpenseRequestDTO request) {
        Purchase purchase = findPurchaseOrThrow(id);
        if (purchase.getStatus() != PurchaseStatus.RECEIVED) {
            throw new PurchaseStateConflictException(
                    "Solo se puede vincular un egreso a una compra en estado RECEIVED. Estado actual: "
                            + purchase.getStatus());
        }
        purchase.setExpenseId(request.expenseId());
        purchase.setStatus(PurchaseStatus.LINKED);
        purchase.setUpdatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    @Override
    public UUID findIdByPurchaseNumber(String purchaseNumber) {
        Purchase purchase = purchaseRepository.findByPurchaseNumber(purchaseNumber)
                .orElseThrow(() -> new PurchaseNumberNotFoundException(purchaseNumber));
        return purchase.getId();
    }

    private Purchase findPurchaseOrThrow(UUID id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
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
