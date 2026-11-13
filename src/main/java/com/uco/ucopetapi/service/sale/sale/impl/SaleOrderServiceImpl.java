package com.uco.ucopetapi.service.sale.sale.impl;

import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.sale.enums.SaleOrderState;
import com.uco.ucopetapi.domain.sale.sale.SaleDeactivateMessageDomain;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderMessageDomain;
import com.uco.ucopetapi.exception.sale.SaleOrderNotFoundException;
import com.uco.ucopetapi.exception.sale.SaleOrderStateConflictException;
import com.uco.ucopetapi.exception.sale.SaleOrderValidationException;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanClient;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanResponse;
import com.uco.ucopetapi.messaging.sale.sale.SaleCreationSenderBroker;
import com.uco.ucopetapi.messaging.sale.sale.SaleDeactivateSenderBroker;
import com.uco.ucopetapi.messaging.sale.sale.SaleUpdateSenderBroker;
import com.uco.ucopetapi.repository.sale.sale.SaleOrderRepository;
import com.uco.ucopetapi.service.sale.itemSale.ItemSaleService;
import com.uco.ucopetapi.service.sale.sale.SaleOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    private static final String ORDER_NUMBER_PREFIX = "OV-";
    private static final int ORDER_NUMBER_DIGITS = 5;
    private static final int COMMENT_MAX_LENGTH = 255;

    private final ItemSaleService itemSaleService;
    private final SaleOrderRepository saleOrderRepository;
    private final HealthPlanClient healthPlanClient;

    private final SaleCreationSenderBroker saleCreationSenderBroker;
    private final SaleUpdateSenderBroker saleUpdateSenderBroker;
    private final SaleDeactivateSenderBroker saleDeactivateSenderBroker;

    public SaleOrderServiceImpl(
            SaleOrderRepository saleOrderRepository,
            HealthPlanClient healthPlanClient,
            ItemSaleService itemSaleService,
            SaleCreationSenderBroker saleCreationSenderBroker,
            SaleUpdateSenderBroker saleUpdateSenderBroker,
            SaleDeactivateSenderBroker saleDeactivateSenderBroker) {

        this.saleOrderRepository = saleOrderRepository;
        this.healthPlanClient = healthPlanClient;
        this.itemSaleService = itemSaleService;
        this.saleCreationSenderBroker = saleCreationSenderBroker;
        this.saleUpdateSenderBroker = saleUpdateSenderBroker;
        this.saleDeactivateSenderBroker = saleDeactivateSenderBroker;
    }

    @Override
    @Transactional
    public SaleOrderDomain registerNewSaleOrder(SaleOrderDomain saleOrder) {

        if (saleOrder == null) {
            throw new SaleOrderValidationException(
                    "La información de la orden de venta es obligatoria."
            );
        }

        validateRequiredReferences(saleOrder);
        validateComment(saleOrder.getComment());
        validateHealthPlan(saleOrder.getHealthPlanId());

        UUID newId = generateUniqueId();

        String newOrderNumber =
                generateNextOrderNumber(saleOrder.getHeadquarterId());

        SaleOrderDomain toPersist = new SaleOrderDomain(
                newId,
                saleOrder.getHeadquarterId(),
                newOrderNumber,
                LocalDateTime.now(ZoneId.of("America/Bogota")),
                saleOrder.getClientID(),
                saleOrder.getPetId(),
                saleOrder.getHealthPlanId(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                saleOrder.getComment(),
                SaleOrderState.BORRADOR
        );

        SaleOrderDomain saved = saleOrderRepository.save(toPersist);

        SaleOrderDomain response = populateItems(saved);

        saleCreationSenderBroker.execute(
                toMessageDomain(response),
                UUID.randomUUID().toString()
        );

        return response;
    }

    @Override
    @Transactional
    public SaleOrderDomain editExistentSaleOrder(
            UUID id,
            SaleOrderDomain saleOrder) {

        if (saleOrder == null) {
            throw new SaleOrderValidationException(
                    "La información para editar la orden es obligatoria."
            );
        }

        SaleOrderDomain existing = findExistingOrThrow(id);

        if (existing.getState() != SaleOrderState.BORRADOR) {
            throw new SaleOrderStateConflictException(
                    "Solo se pueden editar órdenes en estado BORRADOR."
            );
        }

        validateRequiredReferences(saleOrder);
        validateComment(saleOrder.getComment());
        validateHealthPlan(saleOrder.getHealthPlanId());

        existing.setClientID(saleOrder.getClientID());
        existing.setPetId(saleOrder.getPetId());
        existing.setHealthPlanId(saleOrder.getHealthPlanId());
        existing.setComment(saleOrder.getComment());

        SaleOrderDomain saved = saleOrderRepository.save(existing);

        SaleOrderDomain response = populateItems(saved);

        saleUpdateSenderBroker.execute(
                toMessageDomain(response),
                UUID.randomUUID().toString()
        );

        return response;
    }

    @Override
    @Transactional
    public SaleOrderDomain cancelSaleOrder(UUID id) {

        SaleOrderDomain existing = findExistingOrThrow(id);

        if (existing.getState() == SaleOrderState.ANULADA) {
            throw new SaleOrderStateConflictException(
                    "La orden de venta ya se encuentra ANULADA."
            );
        }

        if (existing.getState() == SaleOrderState.FACTURADA) {
            throw new SaleOrderStateConflictException(
                    "No se puede anular desde Ventas una orden ya facturada. "
                            + "Debe coordinarse con el proceso de Facturación."
            );
        }

        if (existing.getState() != SaleOrderState.BORRADOR) {
            throw new SaleOrderStateConflictException(
                    "La orden no se puede anular desde su estado actual."
            );
        }

        existing.setState(SaleOrderState.ANULADA);

        SaleOrderDomain saved = saleOrderRepository.save(existing);

        SaleOrderDomain response = populateItems(saved);

        saleDeactivateSenderBroker.execute(
                new SaleDeactivateMessageDomain(response.getId()),
                UUID.randomUUID().toString()
        );

        return response;
    }

    private SaleOrderDomain populateItems(SaleOrderDomain saleOrder) {

        saleOrder.setItems(
                itemSaleService.listItemsBySaleOrder(saleOrder.getId())
        );

        return saleOrder;
    }

    private SaleOrderMessageDomain toMessageDomain(SaleOrderDomain saleOrder) {

        return new SaleOrderMessageDomain(
                saleOrder.getId(),
                saleOrder.getHeadquarterId(),
                saleOrder.getOrderNumber(),
                saleOrder.getSaleDate(),
                saleOrder.getClientID(),
                saleOrder.getPetId(),
                saleOrder.getHealthPlanId(),
                saleOrder.getSubtotal(),
                saleOrder.getTotalDiscount(),
                saleOrder.getTotalTaxes(),
                saleOrder.getTotal(),
                saleOrder.getComment(),
                saleOrder.getState(),
                saleOrder.getItems()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDomain> findSaleOrderByFilter(
            SaleOrderDomain filter,
            LocalDate dateFrom,
            LocalDate dateTo) {

        if (filter == null) {
            throw new SaleOrderValidationException(
                    "El filtro de búsqueda es obligatorio."
            );
        }

        if (dateFrom != null && dateTo != null
                && dateFrom.isAfter(dateTo)) {

            throw new SaleOrderValidationException(
                    "dateFrom no puede ser posterior a dateTo."
            );
        }

        return saleOrderRepository
                .findByFilter(filter, dateFrom, dateTo)
                .stream()
                .map(this::populateItems)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDomain> findAll() {

        return saleOrderRepository.findAll()
                .stream()
                .map(this::populateItems)
                .toList();
    }

    private SaleOrderDomain findExistingOrThrow(UUID id) {

        return saleOrderRepository.findById(id)
                .orElseThrow(() -> new SaleOrderNotFoundException(
                        "No existe una orden de venta con id " + id
                ));
    }

    private void validateRequiredReferences(SaleOrderDomain saleOrder) {

        UUID defaultUUID = UUIDHelper.getUUIDHelper().getDefault();

        if (saleOrder.getHeadquarterId() == null
                || saleOrder.getHeadquarterId().equals(defaultUUID)) {

            throw new SaleOrderValidationException(
                    "headquarterId es obligatorio."
            );
        }

        if (saleOrder.getClientID() == null
                || saleOrder.getClientID().equals(defaultUUID)) {

            throw new SaleOrderValidationException(
                    "clientID es obligatorio."
            );
        }

        if (saleOrder.getPetId() == null
                || saleOrder.getPetId().equals(defaultUUID)) {

            throw new SaleOrderValidationException(
                    "petId es obligatorio."
            );
        }
    }

    private void validateHealthPlan(UUID healthPlanId) {

        UUID defaultUUID = UUIDHelper.getUUIDHelper().getDefault();

        if (healthPlanId == null || healthPlanId.equals(defaultUUID)) {
            return;
        }

        HealthPlanResponse healthPlan =
                healthPlanClient.getHealthPlan(healthPlanId);

        if (healthPlan == null) {
            throw new SaleOrderValidationException(
                    "No se encontró la póliza indicada."
            );
        }

        if (!"ACTIVA".equalsIgnoreCase(healthPlan.getStatus())) {
            throw new SaleOrderValidationException(
                    "No se puede asociar una póliza que no esté activa."
            );
        }
    }

    private void validateComment(String comment) {

        if (comment != null && comment.length() > COMMENT_MAX_LENGTH) {
            throw new SaleOrderValidationException(
                    "comment no puede superar los "
                            + COMMENT_MAX_LENGTH + " caracteres."
            );
        }
    }

    private UUID generateUniqueId() {

        UUID candidate = UUID.randomUUID();

        while (saleOrderRepository.existsById(candidate)) {
            candidate = UUID.randomUUID();
        }

        return candidate;
    }

    private String generateNextOrderNumber(UUID headquarterId) {

        return saleOrderRepository
                .findLastOrderNumberByHeadquarter(headquarterId)
                .map(last -> increment(last.getOrderNumber()))
                .orElse(format(1));
    }

    private String increment(String lastOrderNumber) {

        String numericPart =
                lastOrderNumber.substring(ORDER_NUMBER_PREFIX.length());

        int next = Integer.parseInt(numericPart) + 1;

        return format(next);
    }

    private String format(int sequence) {

        return ORDER_NUMBER_PREFIX
                + String.format("%0" + ORDER_NUMBER_DIGITS + "d", sequence);
    }
}
