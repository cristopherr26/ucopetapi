package com.uco.ucopetapi.service.sale.impl;

import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import com.uco.ucopetapi.exception.sale.SaleOrderNotFoundException;
import com.uco.ucopetapi.exception.sale.SaleOrderStateConflictException;
import com.uco.ucopetapi.exception.sale.SaleOrderValidationException;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanClient;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanResponse;
import com.uco.ucopetapi.repository.sale.SaleOrderRepository;
import com.uco.ucopetapi.service.sale.SaleOrderService;
import com.uco.ucopetapi.service.sale.ItemSaleService;
import com.uco.ucopetapi.dto.sale.SaleOrderMessageDTO;
import com.uco.ucopetapi.dto.sale.SaleDeactivateMessageDTO;
import com.uco.ucopetapi.messaging.sale.SaleCreationSenderBroker;
import com.uco.ucopetapi.messaging.sale.SaleUpdateSenderBroker;
import com.uco.ucopetapi.messaging.sale.SaleDeactivateSenderBroker;

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
    public SaleOrderDTO registerNewSaleOrder(SaleOrderDTO saleOrderDTO) {

        if (saleOrderDTO == null) {
            throw new SaleOrderValidationException(
                    "La información de la orden de venta es obligatoria."
            );
        }

        validateRequiredReferences(saleOrderDTO);
        validateComment(saleOrderDTO.getComment());
        validateHealthPlan(saleOrderDTO.getHealthPlanId());

        UUID newId = generateUniqueId();

        String newOrderNumber =
                generateNextOrderNumber(saleOrderDTO.getHeadquarterId());

        SaleOrderDTO toPersist = new SaleOrderDTO(
                newId,
                saleOrderDTO.getHeadquarterId(),
                newOrderNumber,
                LocalDateTime.now(ZoneId.of("America/Bogota")),
                saleOrderDTO.getClientID(),
                saleOrderDTO.getPetId(),
                saleOrderDTO.getHealthPlanId(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                saleOrderDTO.getComment(),
                SaleOrderState.BORRADOR
        );

        SaleOrderDomain saved =
                saleOrderRepository.save(toPersist.toDomain());

        SaleOrderDTO response = populateItems(saved.toDTO());

        saleCreationSenderBroker.execute(
                toMessageDTO(response),
                UUID.randomUUID().toString()
        );

        return response;
    }

    @Override
    @Transactional
    public SaleOrderDTO editExistentSaleOrder(
            UUID id,
            SaleOrderDTO saleOrderDTO) {

        if (saleOrderDTO == null) {
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

        validateRequiredReferences(saleOrderDTO);
        validateComment(saleOrderDTO.getComment());
        validateHealthPlan(saleOrderDTO.getHealthPlanId());

        existing.setClientID(saleOrderDTO.getClientID());
        existing.setPetId(saleOrderDTO.getPetId());
        existing.setHealthPlanId(saleOrderDTO.getHealthPlanId());
        existing.setComment(saleOrderDTO.getComment());

        SaleOrderDomain saved = saleOrderRepository.save(existing);

        SaleOrderDTO response = populateItems(saved.toDTO());

        saleUpdateSenderBroker.execute(
                toMessageDTO(response),
                UUID.randomUUID().toString()
        );

        return response;
    }

    @Override
    @Transactional
    public SaleOrderDTO cancelSaleOrder(UUID id) {

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

        SaleOrderDTO response = populateItems(saved.toDTO());

        saleDeactivateSenderBroker.execute(
                new SaleDeactivateMessageDTO(response.getId()),
                UUID.randomUUID().toString()
        );

        return response;
    }

    private SaleOrderDTO populateItems(SaleOrderDTO saleOrderDTO) {
        saleOrderDTO.setItems(
                itemSaleService.listItemsBySaleOrder(saleOrderDTO.getId())
        );

        return saleOrderDTO;
    }

    private SaleOrderMessageDTO toMessageDTO(SaleOrderDTO saleOrderDTO) {
        return new SaleOrderMessageDTO(
                saleOrderDTO.getId(),
                saleOrderDTO.getHeadquarterId(),
                saleOrderDTO.getOrderNumber(),
                saleOrderDTO.getSaleDate(),
                saleOrderDTO.getClientID(),
                saleOrderDTO.getPetId(),
                saleOrderDTO.getHealthPlanId(),
                saleOrderDTO.getSubtotal(),
                saleOrderDTO.getTotalDiscount(),
                saleOrderDTO.getTotalTaxes(),
                saleOrderDTO.getTotal(),
                saleOrderDTO.getComment(),
                saleOrderDTO.getState(),
                saleOrderDTO.getItems()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDTO> findSaleOrderByFilter(
            SaleOrderDTO filter,
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
                .map(SaleOrderDomain::toDTO)
                .map(this::populateItems)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleOrderDTO> findAll() {

        return saleOrderRepository.findAll()
                .stream()
                .map(SaleOrderDomain::toDTO)
                .map(this::populateItems)
                .toList();
    }

    private SaleOrderDomain findExistingOrThrow(UUID id) {

        return saleOrderRepository.findById(id)
                .orElseThrow(() -> new SaleOrderNotFoundException(
                        "No existe una orden de venta con id " + id
                ));
    }

    private void validateRequiredReferences(SaleOrderDTO dto) {

        UUID defaultUUID = UUIDHelper.getUUIDHelper().getDefault();

        if (dto.getHeadquarterId() == null
                || dto.getHeadquarterId().equals(defaultUUID)) {

            throw new SaleOrderValidationException(
                    "headquarterId es obligatorio."
            );
        }

        if (dto.getClientID() == null
                || dto.getClientID().equals(defaultUUID)) {

            throw new SaleOrderValidationException(
                    "clientID es obligatorio."
            );
        }

        if (dto.getPetId() == null
                || dto.getPetId().equals(defaultUUID)) {

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