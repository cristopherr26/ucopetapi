package com.uco.ucopetapi.service.purchases;

import com.uco.ucopetapi.domain.product.enums.StockMovementReason;
import com.uco.ucopetapi.domain.purchases.ItemType;
import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseItem;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
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
import com.uco.ucopetapi.service.headquarter.HeadquarterService;
import com.uco.ucopetapi.service.person.PersonService;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.product.ServiceService;
import com.uco.ucopetapi.service.product.StockService;
import com.uco.ucopetapi.service.provider.ProviderService;
import com.uco.ucopetapi.service.purchases.exception.DuplicatePurchaseNumberException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseNumberNotFoundException;
import com.uco.ucopetapi.service.purchases.exception.PurchaseStateConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
    private final ProviderService providerService;
    private final HeadquarterService headquarterService;
    private final ProductService productService;
    private final ServiceService serviceService;
    private final StockService stockService;
    private final PurchaseCreationSenderBroker purchaseCreationSenderBroker;
    private final PurchaseDeactivationSenderBroker purchaseDeactivationSenderBroker;
    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                                PurchaseItemRepository purchaseItemRepository,
                                PersonService personService,
                                ProviderService providerService,
                                HeadquarterService headquarterService,
                                ProductService productService,
                                ServiceService serviceService,
                                StockService stockService,
                                PurchaseCreationSenderBroker purchaseCreationSenderBroker,
                                PurchaseDeactivationSenderBroker purchaseDeactivationSenderBroker) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.personService = personService;
        this.providerService = providerService;
        this.headquarterService = headquarterService;
        this.productService = productService;
        this.serviceService = serviceService;
        this.stockService = stockService;
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
        addReceivedItemsToStock(purchase);
        purchase.setStatus(PurchaseStatus.RECEIVED);
        purchase.setUpdatedAt(LocalDateTime.now(BOGOTA_ZONE));
        purchase.setUpdatedByPersonId(currentPersonId());
        Purchase saved = purchaseRepository.save(purchase);
        return toResponseDTO(saved);
    }

    private void addReceivedItemsToStock(Purchase purchase) {
        purchaseItemRepository.findByPurchaseId(purchase.getId()).stream()
                .filter(item -> item.getItemType() == ItemType.PRODUCT)
                .forEach(item -> stockService.adjustStock(item.getProductId(), purchase.getHeadquarterId(),
                        item.getQuantity(), StockMovementReason.PURCHASE,
                        "Compra " + purchase.getPurchaseNumber()));
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
        purchase.setUpdatedAt(LocalDateTime.now(BOGOTA_ZONE));
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
        purchase.setUpdatedAt(LocalDateTime.now(BOGOTA_ZONE));
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
        } catch (ResponseStatusException _) {
            return new RelatedEntityDTO(personId, null);
        }
    }

    // Igual que resolvePerson: si el proveedor no aparece (p. ej. datos de
    // prueba inconsistentes) devolvemos el id igual, solo que sin nombre,
    // en vez de tumbar la consulta completa.
    private RelatedEntityDTO resolveSupplier(UUID supplierId) {
        try {
            ProviderDTO provider = providerService.findById(supplierId);
            return new RelatedEntityDTO(supplierId, provider.getProviderName());
        } catch (RuntimeException _) {
            return new RelatedEntityDTO(supplierId, null);
        }
    }

    private RelatedEntityDTO resolveHeadquarter(UUID headquarterId) {
        try {
            HeadquarterDTO headquarter = headquarterService.findById(headquarterId);
            return new RelatedEntityDTO(headquarterId, headquarter.getName());
        } catch (RuntimeException _) {
            return new RelatedEntityDTO(headquarterId, null);
        }
    }

    // Antes esto siempre devolvia el nombre en null: toItemDTO armaba el
    // RelatedEntityDTO directo con (productId, null) sin consultar el
    // catalogo, por eso en el detalle de la compra salia "Item sin nombre".
    // Igual que resolveSupplier/resolveHeadquarter, si no se encuentra se
    // devuelve el id igual, solo que sin nombre.
    private RelatedEntityDTO resolveProduct(UUID productId, ItemType itemType) {
        try {
            String name = itemType == ItemType.SERVICE
                    ? serviceService.getById(productId).getName()
                    : productService.getById(productId, null).getName();
            return new RelatedEntityDTO(productId, name);
        } catch (RuntimeException _) {
            return new RelatedEntityDTO(productId, null);
        }
    }

    private UUID currentPersonId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto de seguridad.");
        }
        return UUID.fromString(authentication.getName());
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
                .hasDiscount(Boolean.TRUE.equals(request.hasDiscount()))
                .purchaseDate(LocalDateTime.now(BOGOTA_ZONE))
                .status(PurchaseStatus.PENDING)
                .createdAt(LocalDateTime.now(BOGOTA_ZONE))
                .updatedAt(LocalDateTime.now(BOGOTA_ZONE))
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
        // El id se genera aqui, igual que el de la compra: el consumidor de
        // ucopet-commercial-processor inserta el item con este mismo id, no
        // llama a purchaseItemRepository.save() de este lado.
        item.setId(UUID.randomUUID());
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
                resolveProduct(item.getProductId(), item.getItemType()),
                item.getItemType(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
