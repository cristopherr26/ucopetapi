package com.uco.ucopetapi.service.sale.itemSale.impl;

import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.sale.enums.ItemType;
import com.uco.ucopetapi.domain.sale.enums.SaleOrderState;
import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDeleteMessageDomain;
import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDomain;
import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleMessageDomain;
import com.uco.ucopetapi.domain.sale.sale.AddItemSaleRequestDomain;
import com.uco.ucopetapi.domain.sale.sale.EditItemSaleRequestDomain;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.exception.sale.SaleOrderNotFoundException;
import com.uco.ucopetapi.exception.sale.SaleOrderStateConflictException;
import com.uco.ucopetapi.exception.sale.SaleOrderValidationException;
import com.uco.ucopetapi.integration.sale.catalog.CatalogClient;
import com.uco.ucopetapi.integration.sale.catalog.CatalogItemResponse;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanClient;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanCoverageResponse;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanResponse;
import com.uco.ucopetapi.messaging.sale.itemSale.ItemSaleCreationSenderBroker;
import com.uco.ucopetapi.messaging.sale.itemSale.ItemSaleDeleteSenderBroker;
import com.uco.ucopetapi.messaging.sale.itemSale.ItemSaleUpdateSenderBroker;
import com.uco.ucopetapi.repository.sale.itemSale.ItemSaleRepository;
import com.uco.ucopetapi.repository.sale.sale.SaleOrderRepository;
import com.uco.ucopetapi.service.sale.itemSale.ItemSaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class ItemSaleServiceImpl implements ItemSaleService {

    private final ItemSaleRepository itemSaleRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final CatalogClient catalogClient;
    private final HealthPlanClient healthPlanClient;

    private final ItemSaleCreationSenderBroker itemSaleCreationSenderBroker;
    private final ItemSaleUpdateSenderBroker itemSaleUpdateSenderBroker;
    private final ItemSaleDeleteSenderBroker itemSaleDeleteSenderBroker;

    public ItemSaleServiceImpl(
            ItemSaleRepository itemSaleRepository,
            SaleOrderRepository saleOrderRepository,
            CatalogClient catalogClient,
            HealthPlanClient healthPlanClient,
            ItemSaleCreationSenderBroker itemSaleCreationSenderBroker,
            ItemSaleUpdateSenderBroker itemSaleUpdateSenderBroker,
            ItemSaleDeleteSenderBroker itemSaleDeleteSenderBroker) {

        this.itemSaleRepository = itemSaleRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.catalogClient = catalogClient;
        this.healthPlanClient = healthPlanClient;
        this.itemSaleCreationSenderBroker = itemSaleCreationSenderBroker;
        this.itemSaleUpdateSenderBroker = itemSaleUpdateSenderBroker;
        this.itemSaleDeleteSenderBroker = itemSaleDeleteSenderBroker;
    }

    @Override
    @Transactional
    public SaleOrderDomain addItemToSaleOrder(
            UUID saleOrderId,
            AddItemSaleRequestDomain request) {

        SaleOrderDomain order = findOrderOrThrow(saleOrderId);

        requireBorrador(order);
        validateAddRequest(request);

        CatalogItemResponse catalogItem = resolveCatalogItem(
                request.getItemId(),
                request.getItemType(),
                order.getHeadquarterId()
        );

        validateCatalogItem(catalogItem, request.getItemId());

        if (request.getItemType() == ItemType.SERVICIO) {
            boolean alreadyAdded = itemSaleRepository
                    .findBySaleOrderId(saleOrderId)
                    .stream()
                    .anyMatch(item ->
                            item.getItemType() == ItemType.SERVICIO
                                    && item.getItemId().equals(request.getItemId()));

            if (alreadyAdded) {
                throw new SaleOrderValidationException(
                        "El servicio ya está incluido en esta orden de venta."
                );
            }
        }

        BigDecimal unitPrice = BigDecimal.valueOf(catalogItem.getPrice());
        int quantity = request.getQuantity();

        BigDecimal subtotal = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        ItemSaleDomain newItem = new ItemSaleDomain(
                UUID.randomUUID(),
                saleOrderId,
                request.getItemId(),
                request.getItemType(),
                catalogItem.getTaxCategory(),
                quantity,
                unitPrice,
                subtotal
        );

        ItemSaleDomain savedItem = itemSaleRepository.save(newItem);

        recalculateOrderTotals(order);

        SaleOrderDomain savedOrder = saleOrderRepository.save(order);

        itemSaleCreationSenderBroker.execute(
                toMessageDomain(savedItem),
                UUID.randomUUID().toString()
        );

        return populateItems(savedOrder);
    }

    @Override
    @Transactional
    public SaleOrderDomain editItemInSaleOrder(
            UUID saleOrderId,
            UUID itemSaleId,
            EditItemSaleRequestDomain request) {

        SaleOrderDomain order = findOrderOrThrow(saleOrderId);

        requireBorrador(order);
        validateEditRequest(request);

        ItemSaleDomain item = itemSaleRepository.findById(itemSaleId)
                .orElseThrow(() -> new SaleOrderValidationException(
                        "No existe un artículo de venta con id " + itemSaleId
                ));

        if (!item.getSaleOrderId().equals(saleOrderId)) {
            throw new SaleOrderValidationException(
                    "El artículo no pertenece a la orden de venta indicada."
            );
        }

        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        item.setQuantity(request.getQuantity());
        item.setSubtotal(subtotal);

        ItemSaleDomain savedItem = itemSaleRepository.save(item);

        recalculateOrderTotals(order);

        SaleOrderDomain savedOrder = saleOrderRepository.save(order);

        itemSaleUpdateSenderBroker.execute(
                toMessageDomain(savedItem),
                UUID.randomUUID().toString()
        );

        return populateItems(savedOrder);
    }

    @Override
    @Transactional
    public SaleOrderDomain removeItemFromSaleOrder(
            UUID saleOrderId,
            UUID itemSaleId) {

        SaleOrderDomain order = findOrderOrThrow(saleOrderId);

        requireBorrador(order);

        ItemSaleDomain item = itemSaleRepository.findById(itemSaleId)
                .orElseThrow(() -> new SaleOrderValidationException(
                        "No existe un artículo de venta con id " + itemSaleId
                ));

        if (!item.getSaleOrderId().equals(saleOrderId)) {
            throw new SaleOrderValidationException(
                    "El artículo no pertenece a la orden de venta indicada."
            );
        }

        itemSaleRepository.deleteById(itemSaleId);

        recalculateOrderTotals(order);

        SaleOrderDomain savedOrder = saleOrderRepository.save(order);

        itemSaleDeleteSenderBroker.execute(
                toDeleteMessageDomain(item),
                UUID.randomUUID().toString()
        );

        return populateItems(savedOrder);
    }

    private SaleOrderDomain populateItems(SaleOrderDomain saleOrder) {

        saleOrder.setItems(
                itemSaleRepository.findBySaleOrderId(saleOrder.getId())
        );

        return saleOrder;
    }

    private ItemSaleMessageDomain toMessageDomain(ItemSaleDomain item) {

        return new ItemSaleMessageDomain(
                item.getId(),
                item.getSaleOrderId(),
                item.getItemId(),
                item.getItemType().name(),
                item.getTaxCategory().name(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private ItemSaleDeleteMessageDomain toDeleteMessageDomain(
            ItemSaleDomain item) {

        return new ItemSaleDeleteMessageDomain(
                item.getId(),
                item.getSaleOrderId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemSaleDomain> listItemsBySaleOrder(UUID saleOrderId) {

        findOrderOrThrow(saleOrderId);

        return itemSaleRepository.findBySaleOrderId(saleOrderId);
    }

    private SaleOrderDomain findOrderOrThrow(UUID saleOrderId) {

        return saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new SaleOrderNotFoundException(
                        "No existe una orden de venta con id " + saleOrderId
                ));
    }

    private void requireBorrador(SaleOrderDomain order) {

        if (order.getState() != SaleOrderState.BORRADOR) {
            throw new SaleOrderStateConflictException(
                    "Solo se pueden modificar artículos de una orden en estado BORRADOR."
            );
        }
    }

    private void validateAddRequest(AddItemSaleRequestDomain request) {

        if (request == null) {
            throw new SaleOrderValidationException(
                    "La solicitud para agregar el artículo es obligatoria."
            );
        }

        if (request.getItemId() == null) {
            throw new SaleOrderValidationException("itemId es obligatorio.");
        }

        if (request.getItemType() == null) {
            throw new SaleOrderValidationException(
                    "itemType debe ser PRODUCTO o SERVICIO."
            );
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new SaleOrderValidationException(
                    "quantity debe ser mayor que cero."
            );
        }
    }

    private void validateEditRequest(EditItemSaleRequestDomain request) {

        if (request == null) {
            throw new SaleOrderValidationException(
                    "La solicitud para editar el artículo es obligatoria."
            );
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new SaleOrderValidationException(
                    "quantity debe ser mayor que cero."
            );
        }
    }

    private CatalogItemResponse resolveCatalogItem(
            UUID itemId,
            ItemType itemType,
            UUID headquarterId) {

        CatalogItemResponse response;

        if (itemType == ItemType.PRODUCTO) {
            response = catalogClient.getProduct(itemId, headquarterId);
        } else {
            response = catalogClient.getService(itemId);
        }

        if (response == null) {
            throw new SaleOrderValidationException(
                    "El catálogo no devolvió información para el artículo " + itemId
            );
        }

        if (itemType == ItemType.SERVICIO) {
            List<UUID> headquarters = response.getHeadquarterIds();

            if (headquarters == null || !headquarters.contains(headquarterId)) {
                throw new SaleOrderValidationException(
                        "El servicio no está ofrecido en la sede de la orden."
                );
            }
        }

        return response;
    }

    private void validateCatalogItem(
            CatalogItemResponse item,
            UUID itemId) {

        if (!Boolean.TRUE.equals(item.getActive())
                || !Boolean.TRUE.equals(item.getSellable())) {

            throw new SaleOrderValidationException(
                    "El artículo " + itemId + " no está disponible para la venta."
            );
        }

        if (item.getPrice() == null || item.getPrice() < 0) {
            throw new SaleOrderValidationException(
                    "El catálogo devolvió un precio inválido para el artículo " + itemId
            );
        }

        if (item.getTaxCategory() == null) {
            throw new SaleOrderValidationException(
                    "El artículo no tiene una categoría de impuesto válida."
            );
        }
    }

    private void recalculateOrderTotals(SaleOrderDomain order) {

        List<ItemSaleDomain> items =
                itemSaleRepository.findBySaleOrderId(order.getId());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;

        for (ItemSaleDomain item : items) {
            BigDecimal itemSubtotal = item.getSubtotal();

            subtotal = subtotal.add(itemSubtotal);

            BigDecimal percentage = BigDecimal.valueOf(
                    item.getTaxCategory().getPercentage()
            );

            BigDecimal itemTaxes = itemSubtotal
                    .multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalTaxes = totalTaxes.add(itemTaxes);
        }

        BigDecimal totalDiscount = calculateTotalDiscount(
                order.getHealthPlanId(),
                items
        );

        BigDecimal total = subtotal
                .subtract(totalDiscount)
                .add(totalTaxes)
                .setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setTotalTaxes(totalTaxes.setScale(2, RoundingMode.HALF_UP));
        order.setTotalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP));
        order.setTotal(total);
    }

    private BigDecimal calculateTotalDiscount(
            UUID healthPlanId,
            List<ItemSaleDomain> items) {

        UUID defaultUUID = UUIDHelper.getUUIDHelper().getDefault();

        if (healthPlanId == null || healthPlanId.equals(defaultUUID)) {
            return BigDecimal.ZERO;
        }

        HealthPlanResponse healthPlan =
                healthPlanClient.getHealthPlan(healthPlanId);

        if (healthPlan == null) {
            throw new SaleOrderValidationException(
                    "No se encontró información de la póliza asociada a la venta."
            );
        }

        if (!"ACTIVA".equalsIgnoreCase(healthPlan.getStatus())) {
            throw new SaleOrderValidationException(
                    "La póliza asociada a la venta no está activa."
            );
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (ItemSaleDomain item : items) {
            if (item.getItemType() != ItemType.SERVICIO) {
                continue;
            }

            HealthPlanCoverageResponse coverage =
                    findCoverageForService(healthPlan, item.getItemId());

            if (coverage == null) {
                continue;
            }

            totalDiscount = totalDiscount.add(
                    calculateLineDiscount(item, coverage)
            );
        }

        return totalDiscount.setScale(2, RoundingMode.HALF_UP);
    }

    private HealthPlanCoverageResponse findCoverageForService(
            HealthPlanResponse healthPlan,
            UUID serviceId) {

        if (healthPlan.getCoverages() == null) {
            return null;
        }

        return healthPlan.getCoverages()
                .stream()
                .filter(coverage ->
                        coverage.getServiceId() != null
                                && coverage.getServiceId().equals(serviceId))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal calculateLineDiscount(
            ItemSaleDomain item,
            HealthPlanCoverageResponse coverage) {

        if (coverage.getCoveragePercentage() == null
                || coverage.getCoverageLimit() == null
                || coverage.getCoveragePercentage() < 0
                || coverage.getCoveragePercentage() > 100
                || coverage.getCoverageLimit().signum() < 0) {

            throw new SaleOrderValidationException(
                    "La cobertura de la póliza contiene valores inválidos."
            );
        }

        BigDecimal percentage = BigDecimal.valueOf(
                coverage.getCoveragePercentage()
        ).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal lineSubtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        BigDecimal calculatedDiscount = lineSubtotal
                .multiply(percentage)
                .setScale(2, RoundingMode.HALF_UP);

        return calculatedDiscount.min(coverage.getCoverageLimit());
    }
}
