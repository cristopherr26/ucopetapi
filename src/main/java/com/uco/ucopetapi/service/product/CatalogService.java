package com.uco.ucopetapi.service.product;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.CatalogItemDomain;
import com.uco.ucopetapi.domain.product.CatalogItemHeadquarterDomain;
import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.messaging.inventory.InventoryCommand;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.ICatalogItemHeadquarterRepository;
import com.uco.ucopetapi.repository.product.ICatalogItemRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;

@Service
public class CatalogService {

    private final ICatalogItemRepository itemRepository;
    private final ICatalogItemHeadquarterRepository itemHeadquarterRepository;
    private final IStockRepository stockRepository;
    private final HeadquarterRepository headquarterRepository;
    private final MessageSender<InventoryCommand> messageSender;

    public CatalogService(ICatalogItemRepository itemRepository,
                          ICatalogItemHeadquarterRepository itemHeadquarterRepository,
                          IStockRepository stockRepository,
                          HeadquarterRepository headquarterRepository,
                          MessageSender<InventoryCommand> messageSender) {
        this.itemRepository = itemRepository;
        this.itemHeadquarterRepository = itemHeadquarterRepository;
        this.stockRepository = stockRepository;
        this.headquarterRepository = headquarterRepository;
        this.messageSender = messageSender;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemDTO> list(CatalogItemType type, CatalogCategory category, Boolean active,
                                     Boolean purchasable, Boolean sellable, TaxCategory taxCategory,
                                     UUID headquarterId) {
        Objects.requireNonNull(type, "El tipo es obligatorio; para la lista mixta se usa listAll");
        return listItems(type, category, active, purchasable, sellable, taxCategory, headquarterId);
    }

    @Transactional(readOnly = true)
    public List<CatalogItemDTO> listAll(CatalogCategory category, Boolean active, Boolean sellable,
                                        TaxCategory taxCategory, UUID headquarterId) {
        return listItems(null, category, active, null, sellable, taxCategory, headquarterId).stream()
                .sorted(Comparator.comparing(CatalogItemDTO::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<CatalogItemDTO> listItems(CatalogItemType type, CatalogCategory category, Boolean active,
                                           Boolean purchasable, Boolean sellable, TaxCategory taxCategory,
                                           UUID headquarterId) {
        requireExistingHeadquarter(headquarterId);
        List<CatalogItemDomain> items = itemRepository.findByFilter(type, category, active, purchasable,
                sellable, taxCategory, headquarterId);
        if (items.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = items.stream().map(CatalogItemDomain::getId).toList();
        Map<UUID, List<UUID>> headquarters = type != CatalogItemType.PRODUCT
                ? itemHeadquarterRepository.findByItem_IdIn(ids).stream()
                        .collect(Collectors.groupingBy(association -> association.getItem().getId(),
                                Collectors.mapping(association -> association.getHeadquarter().getId(),
                                        Collectors.toList())))
                : Map.of();
        Map<UUID, Integer> stock = type != CatalogItemType.SERVICE && headquarterId != null
                ? stockRepository.findByItem_IdInAndHeadquarter_Id(ids, headquarterId).stream()
                        .collect(Collectors.toMap(row -> row.getItem().getId(), StockDomain::getQuantity))
                : Map.of();
        return items.stream()
                .map(item -> toDto(item,
                        item.getType() == CatalogItemType.SERVICE
                                ? headquarters.getOrDefault(item.getId(), List.of()) : null,
                        item.getType() == CatalogItemType.PRODUCT && headquarterId != null
                                ? stock.getOrDefault(item.getId(), 0) : null))
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogItemDTO getById(CatalogItemType type, UUID id, UUID headquarterId) {
        CatalogItemDomain item = findOrThrow(type, id);
        requireExistingHeadquarter(headquarterId);
        return toDto(item, headquarterId);
    }

    @Transactional
    public CatalogItemDTO create(CatalogItemType type, CatalogItemDTO request) {
        prepareCreate(type, request);
        CatalogItemDomain item = saveOrConflict(type, new CatalogItemDomain(
                UUID.randomUUID(), type, request.getName(), request.getDescription(), request.getImageUrl(),
                request.getPrice(), request.getTaxCategory(), request.getSellable(), true,
                request.getCategory(), type == CatalogItemType.SERVICE ? request.getPurchasable() : null));
        if (type == CatalogItemType.SERVICE) {
            saveHeadquarters(item, request.getHeadquarterIds());
        }
        return toDto(item, null);
    }

    @Transactional
    public CatalogItemDTO update(CatalogItemType type, UUID id, CatalogItemDTO request) {
        CatalogItemDomain item = validateForUpdate(type, id, request);

        CatalogItems.applyCommonFields(item, request.getName(), request.getDescription(), request.getImageUrl(),
                request.getPrice(), request.getTaxCategory(), request.getSellable());
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (type == CatalogItemType.SERVICE && request.getPurchasable() != null) {
            item.setPurchasable(request.getPurchasable());
        }
        CatalogItems.applyPriceConsistency(item, label(type));
        item = saveOrConflict(type, item);

        if (type == CatalogItemType.SERVICE && request.getHeadquarterIds() != null) {
            itemHeadquarterRepository.deleteByItem_Id(item.getId());
            saveHeadquarters(item, request.getHeadquarterIds());
        }
        return toDto(item, null);
    }

    @Transactional
    public void deactivate(CatalogItemType type, UUID id) {
        CatalogItemDomain item = findOrThrow(type, id);
        item.setActive(false);
        itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public CatalogItemDTO prepareCreate(CatalogItemType type, CatalogItemDTO request) {
        request.setName(CatalogItems.normalizeName(request.getName()));
        validateForCreate(type, request);
        requireExistingReferences(type, request);
        return request;
    }

    @Transactional(readOnly = true)
    public void prepareUpdate(CatalogItemType type, UUID id, CatalogItemDTO request) {
        CatalogItemDomain item = validateForUpdate(type, id, request);
        Boolean sellable = request.getSellable() != null ? request.getSellable() : item.getSellable();
        if (Boolean.TRUE.equals(sellable)) {
            CatalogValidationUtils.requirePriceWhenSellable(true,
                    request.getPrice() != null ? request.getPrice() : item.getPrice(), label(type));
        }
        requireExistingReferences(type, request);
    }

    @Transactional(readOnly = true)
    public void prepareDeactivate(CatalogItemType type, UUID id) {
        findOrThrow(type, id);
    }

    public CatalogItemDTO submitCreate(CatalogItemType type, CatalogItemDTO request) {
        CatalogItemDTO prepared = prepareCreate(type, request);
        UUID id = UUID.randomUUID();
        messageSender.execute(new InventoryCommand(command(type, "create"), itemCommand(id, type, prepared)),
                UUID.randomUUID().toString());
        return CatalogItemDTO.builder()
                .id(id)
                .type(type)
                .name(prepared.getName())
                .description(prepared.getDescription())
                .imageUrl(prepared.getImageUrl())
                .price(prepared.getPrice())
                .taxCategory(prepared.getTaxCategory())
                .sellable(prepared.getSellable())
                .active(true)
                .category(prepared.getCategory())
                .purchasable(type == CatalogItemType.SERVICE ? prepared.getPurchasable() : null)
                .headquarterIds(type == CatalogItemType.SERVICE ? prepared.getHeadquarterIds() : null)
                .build();
    }

    public void submitUpdate(CatalogItemType type, UUID id, CatalogItemDTO request) {
        prepareUpdate(type, id, request);
        messageSender.execute(new InventoryCommand(command(type, "update"), itemCommand(id, type, request)),
                UUID.randomUUID().toString());
    }

    public void submitDeactivate(CatalogItemType type, UUID id) {
        prepareDeactivate(type, id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        messageSender.execute(new InventoryCommand(command(type, "deactivate"), data), UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID id) {
        return Boolean.TRUE.equals(findOrThrow(CatalogItemType.SERVICE, id).getActive());
    }

    @Transactional(readOnly = true)
    public long countActiveServices() {
        return itemRepository.countByTypeAndActiveTrue(CatalogItemType.SERVICE);
    }

    private void validateForCreate(CatalogItemType type, CatalogItemDTO request) {
        CatalogValidationUtils.requirePriceWhenSellable(request.getSellable(), request.getPrice(), label(type));
        CatalogValidationUtils.rejectPriceWhenNotSellable(request.getSellable(), request.getPrice(), label(type));
        if (type == CatalogItemType.SERVICE) {
            if (request.getPurchasable() == null) {
                throw new IllegalArgumentException(
                        "Debes indicar si el servicio es comprable a un proveedor externo");
            }
        }
        if (itemRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateCatalogNameException(label(type));
        }
        CatalogValidationUtils.validateNoDuplicateHeadquarters(request.getHeadquarterIds());
    }

    private CatalogItemDomain validateForUpdate(CatalogItemType type, UUID id, CatalogItemDTO request) {
        request.setName(CatalogItems.normalizeName(request.getName()));
        CatalogValidationUtils.validatePriceOnUpdate(request.getPrice());
        CatalogValidationUtils.validateNoDuplicateHeadquarters(request.getHeadquarterIds());

        CatalogItemDomain item = findOrThrow(type, id);
        CatalogValidationUtils.rejectPriceWhenNotSellable(
                request.getSellable() != null ? request.getSellable() : item.getSellable(),
                request.getPrice(), label(type));
        if (request.getName() != null && itemRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateCatalogNameException(label(type));
        }
        return item;
    }

    private void requireExistingReferences(CatalogItemType type, CatalogItemDTO request) {
        if (type == CatalogItemType.SERVICE && request.getHeadquarterIds() != null) {
            for (UUID headquarterId : request.getHeadquarterIds()) {
                requireExistingHeadquarter(headquarterId);
            }
        }
    }

    private static String command(CatalogItemType type, String action) {
        return (type == CatalogItemType.SERVICE ? "service" : "product") + "-" + action;
    }

    private static Map<String, Object> itemCommand(UUID id, CatalogItemType type, CatalogItemDTO request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("name", request.getName());
        data.put("description", request.getDescription());
        data.put("imageUrl", request.getImageUrl());
        data.put("price", request.getPrice());
        data.put("taxCategory", request.getTaxCategory());
        data.put("sellable", request.getSellable());
        data.put("category", request.getCategory());
        if (type == CatalogItemType.SERVICE) {
            data.put("purchasable", request.getPurchasable());
            data.put("headquarterIds", request.getHeadquarterIds());
        }
        return data;
    }

    private void saveHeadquarters(CatalogItemDomain item, List<UUID> headquarterIds) {
        if (headquarterIds == null) {
            return;
        }
        for (UUID headquarterId : headquarterIds) {
            HeadquarterDomain headquarter = headquarterRepository.findById(headquarterId)
                    .orElseThrow(() -> new NoSuchElementException("Sede no encontrada: " + headquarterId));
            itemHeadquarterRepository.save(
                    new CatalogItemHeadquarterDomain(UUID.randomUUID(), item, headquarter));
        }
    }

    private CatalogItemDomain saveOrConflict(CatalogItemType type, CatalogItemDomain item) {
        try {
            return itemRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException nameTakenBetweenCheckAndInsert) {
            throw new DuplicateCatalogNameException(label(type));
        }
    }

    private CatalogItemDomain findOrThrow(CatalogItemType type, UUID id) {
        return itemRepository.findByIdAndType(id, type)
                .orElseThrow(() -> new NoSuchElementException(capitalize(label(type)) + " no encontrado: " + id));
    }

    private void requireExistingHeadquarter(UUID headquarterId) {
        if (headquarterId != null && !headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
    }

    private CatalogItemDTO toDto(CatalogItemDomain item, UUID headquarterId) {
        List<UUID> headquarters = item.getType() == CatalogItemType.SERVICE
                ? itemHeadquarterRepository.findByItem_Id(item.getId()).stream()
                        .map(association -> association.getHeadquarter().getId())
                        .toList()
                : null;
        Integer stockAtLocation = item.getType() == CatalogItemType.PRODUCT && headquarterId != null
                ? stockRepository.findByItem_IdAndHeadquarter_Id(item.getId(), headquarterId)
                        .map(StockDomain::getQuantity)
                        .orElse(0)
                : null;
        return toDto(item, headquarters, stockAtLocation);
    }

    private static CatalogItemDTO toDto(CatalogItemDomain item, List<UUID> headquarterIds,
                                        Integer stockAtLocation) {
        return CatalogItemDTO.builder()
                .id(item.getId())
                .type(item.getType())
                .name(item.getName())
                .description(item.getDescription())
                .imageUrl(item.getImageUrl())
                .price(item.getPrice())
                .taxCategory(item.getTaxCategory())
                .sellable(item.getSellable())
                .active(item.getActive())
                .category(item.getCategory())
                .purchasable(item.getPurchasable())
                .headquarterIds(headquarterIds)
                .stockAtLocation(stockAtLocation)
                .build();
    }

    private static String label(CatalogItemType type) {
        return type == CatalogItemType.SERVICE ? CatalogValidationUtils.SERVICE : CatalogValidationUtils.PRODUCT;
    }

    private static String capitalize(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }
}
