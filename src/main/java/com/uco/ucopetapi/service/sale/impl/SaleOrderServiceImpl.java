package com.uco.ucopetapi.service.sale.impl;

import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import com.uco.ucopetapi.repository.sale.SaleOrderRepository;
import com.uco.ucopetapi.service.sale.SaleOrderService;
import com.uco.ucopetapi.service.sale.exception.SaleOrderNotFoundException;
import com.uco.ucopetapi.service.sale.exception.SaleOrderStateConflictException;
import com.uco.ucopetapi.service.sale.exception.SaleOrderValidationException;
import org.springframework.stereotype.Service;

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

    private final SaleOrderRepository saleOrderRepository;

    public SaleOrderServiceImpl(SaleOrderRepository saleOrderRepository) {
        this.saleOrderRepository = saleOrderRepository;
    }

    @Override
    public SaleOrderDTO registerNewSaleOrder(SaleOrderDTO saleOrderDTO) {
        validateRequiredReferences(saleOrderDTO);
        validateComment(saleOrderDTO.getComment());

        UUID newId = generateUniqueId();
        String newOrderNumber = generateNextOrderNumber(saleOrderDTO.getHeadquarterId());
        SaleOrderDTO toPersist = new SaleOrderDTO(
                newId,
                saleOrderDTO.getHeadquarterId(),
                newOrderNumber,
                LocalDateTime.now(ZoneId.of("America/Bogota")),
                saleOrderDTO.getClientID(),
                saleOrderDTO.getPetId(),
                saleOrderDTO.getHealthPlanId(),
                0,
                0,
                0,
                0,
                saleOrderDTO.getComment(),
                SaleOrderState.BORRADOR
        );

        SaleOrderDomain saved = saleOrderRepository.save(toPersist.toDomain());
        return saved.toDTO();
    }

    @Override
    public SaleOrderDTO editExistentSaleOrder(UUID id, SaleOrderDTO saleOrderDTO) {
        SaleOrderDomain existing = findExistingOrThrow(id);

        if (existing.getState() == SaleOrderState.FACTURADA || existing.getState() == SaleOrderState.ANULADA) {
            throw new SaleOrderStateConflictException(
                    "No se puede editar una orden de venta en estado " + existing.getState()
                            + ". Solo las órdenes en BORRADOR son editables; use cancelSaleOrder para anular.");
        }

        SaleOrderState requestedState = saleOrderDTO.getState();
        if (requestedState == SaleOrderState.SIN_ESTADO) {
            requestedState = existing.getState();
        }
        if (requestedState != SaleOrderState.BORRADOR && requestedState != SaleOrderState.FACTURADA) {
            throw new SaleOrderStateConflictException(
                    "Desde BORRADOR solo se permite mantener BORRADOR o pasar a FACTURADO. "
                            + "Para anular use cancelSaleOrder.");
        }

        validateComment(saleOrderDTO.getComment());

        existing.setClientID(saleOrderDTO.getClientID());
        existing.setPetId(saleOrderDTO.getPetId());
        existing.setHealthPlanId(saleOrderDTO.getHealthPlanId());
        existing.setSubtotal(saleOrderDTO.getSubtotal());
        existing.setTotalDiscount(saleOrderDTO.getTotalDiscount());
        existing.setTotalTaxes(saleOrderDTO.getTotalTaxes());
        existing.setTotal(saleOrderDTO.getTotal());
        existing.setComment(saleOrderDTO.getComment());
        existing.setState(requestedState);

        SaleOrderDomain saved = saleOrderRepository.save(existing);
        return saved.toDTO();
    }

    @Override
    public SaleOrderDTO cancelSaleOrder(UUID id) {
        SaleOrderDomain existing = findExistingOrThrow(id);

        if (existing.getState() == SaleOrderState.ANULADA) {
            throw new SaleOrderStateConflictException("La orden de venta ya se encuentra ANULADA.");
        }

        existing.setState(SaleOrderState.ANULADA);
        SaleOrderDomain saved = saleOrderRepository.save(existing);
        return saved.toDTO();
    }

    @Override
    public List<SaleOrderDTO> findSaleOrderByFilter(SaleOrderDTO filter, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new SaleOrderValidationException("dateFrom no puede ser posterior a dateTo.");
        }

        return saleOrderRepository.findByFilter(filter, dateFrom, dateTo).stream()
                .map(SaleOrderDomain::toDTO)
                .toList();
    }

    @Override
    public List<SaleOrderDTO> findAll() {
        return saleOrderRepository.findAll().stream()
                .map(SaleOrderDomain::toDTO)
                .toList();
    }

    private SaleOrderDomain findExistingOrThrow(UUID id) {
        return saleOrderRepository.findById(id)
                .orElseThrow(() -> new SaleOrderNotFoundException("No existe una orden de venta con id " + id));
    }

    private void validateRequiredReferences(SaleOrderDTO dto) {
        UUID defaultUUID = UUIDHelper.getUUIDHelper().getDefault();

        if (dto.getHeadquarterId() == null || dto.getHeadquarterId().equals(defaultUUID)) {
            throw new SaleOrderValidationException("headquarterId es obligatorio.");
        }
        if (dto.getClientID() == null || dto.getClientID().equals(defaultUUID)) {
            throw new SaleOrderValidationException("clientID es obligatorio.");
        }
        if (dto.getPetId() == null || dto.getPetId().equals(defaultUUID)) {
            throw new SaleOrderValidationException("petId es obligatorio.");
        }
    }

    private void validateComment(String comment) {
        if (comment != null && comment.length() > COMMENT_MAX_LENGTH) {
            throw new SaleOrderValidationException(
                    "comment no puede superar los " + COMMENT_MAX_LENGTH + " caracteres.");
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
        return saleOrderRepository.findLastOrderNumberByHeadquarter(headquarterId)
                .map(last -> increment(last.getOrderNumber()))
                .orElse(format(1));
    }

    private String increment(String lastOrderNumber) {
        String numericPart = lastOrderNumber.substring(ORDER_NUMBER_PREFIX.length());
        int next = Integer.parseInt(numericPart) + 1;
        return format(next);
    }

    private String format(int sequence) {
        return ORDER_NUMBER_PREFIX + String.format("%0" + ORDER_NUMBER_DIGITS + "d", sequence);
    }
}
