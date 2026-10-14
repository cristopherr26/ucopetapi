package com.uco.ucopetapi.service.invoice.impl;

import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.invoice.InvoiceDomain;
import com.uco.ucopetapi.domain.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;
import com.uco.ucopetapi.dto.invoice.enums.InvoiceState;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import com.uco.ucopetapi.repository.invoice.InvoiceRepository;
import com.uco.ucopetapi.repository.sale.SaleOrderRepository;
import com.uco.ucopetapi.service.invoice.InvoiceService;
import com.uco.ucopetapi.service.invoice.exception.InvoiceNotFoundException;
import com.uco.ucopetapi.service.invoice.exception.InvoiceStateConflictException;
import com.uco.ucopetapi.service.invoice.exception.InvoiceValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final String INVOICE_NUMBER_PREFIX = "FV-";
    private static final int INVOICE_NUMBER_DIGITS = 5;
    private static final String BOGOTA = "America/Bogota";

    private final InvoiceRepository invoiceRepository;
    private final SaleOrderRepository saleOrderRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, SaleOrderRepository saleOrderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.saleOrderRepository = saleOrderRepository;
    }

    @Override
    public InvoiceDTO registerNewInvoice(CreateInvoiceRequestDTO request) {
        UUID defaultUUID = UUIDHelper.getUUIDHelper().getDefault();
        if (request.getSaleOrderId() == null || request.getSaleOrderId().equals(defaultUUID)) {
            throw new InvoiceValidationException("saleOrderId es obligatorio.");
        }

        SaleOrderDomain saleOrder = saleOrderRepository.findById(request.getSaleOrderId())
                .orElseThrow(() -> new InvoiceValidationException(
                        "No existe una orden de venta con id " + request.getSaleOrderId()));

        if (invoiceRepository.existsBySaleOrderId(saleOrder.getId())) {
            throw new InvoiceStateConflictException(
                    "La orden de venta " + saleOrder.getOrderNumber() + " ya tiene una factura asociada.");
        }

        if (saleOrder.getState() != SaleOrderState.BORRADOR) {
            throw new InvoiceStateConflictException(
                    "Solo se puede facturar una orden de venta en estado BORRADOR. Estado actual: "
                            + saleOrder.getState());
        }

        UUID newId = generateUniqueId();
        String newInvoiceNumber = generateNextInvoiceNumber(saleOrder.getHeadquarterId());

        InvoiceDTO toPersist = new InvoiceDTO(
                newId,
                saleOrder.getId(),
                newInvoiceNumber,
                saleOrder.getHeadquarterId(),
                saleOrder.getClientID(),
                saleOrder.getPetId(),
                LocalDateTime.now(ZoneId.of(BOGOTA)),
                saleOrder.getTotal(),
                0,
                saleOrder.getTotal(),
                request.getObservations(),
                InvoiceState.PENDIENTE
        );

        InvoiceDomain saved = invoiceRepository.save(toPersist.toDomain());

        saleOrder.setState(SaleOrderState.FACTURADA);
        saleOrderRepository.save(saleOrder);

        return saved.toDTO();
    }

    @Override
    public InvoiceDTO registerPayment(UUID id, RegisterPaymentRequestDTO payment) {
        InvoiceDomain existing = findExistingOrThrow(id);

        if (existing.getState() == InvoiceState.ANULADA) {
            throw new InvoiceStateConflictException("No se puede registrar un pago sobre una factura ANULADA.");
        }
        if (existing.getState() == InvoiceState.PAGADA) {
            throw new InvoiceStateConflictException("La factura " + existing.getInvoiceNumber()
                    + " ya se encuentra completamente pagada.");
        }
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            throw new InvoiceValidationException("amount debe ser mayor a cero.");
        }

        int newPaidAmount = existing.getPaidAmount() + payment.getAmount();
        if (newPaidAmount > existing.getTotal()) {
            throw new InvoiceValidationException(
                    "El pago excede el saldo pendiente. Saldo actual: " + existing.getBalance());
        }

        int newBalance = existing.getTotal() - newPaidAmount;

        existing.setPaidAmount(newPaidAmount);
        existing.setBalance(newBalance);
        existing.setState(newBalance == 0 ? InvoiceState.PAGADA : InvoiceState.PARCIALMENTE_PAGADA);

        InvoiceDomain saved = invoiceRepository.save(existing);
        return saved.toDTO();
    }

    @Override
    public InvoiceDTO cancelInvoice(UUID id) {
        InvoiceDomain existing = findExistingOrThrow(id);

        if (existing.getState() == InvoiceState.ANULADA) {
            throw new InvoiceStateConflictException("La factura " + existing.getInvoiceNumber()
                    + " ya se encuentra ANULADA.");
        }

        existing.setState(InvoiceState.ANULADA);
        InvoiceDomain saved = invoiceRepository.save(existing);
        return saved.toDTO();
    }

    @Override
    public InvoiceDTO findById(UUID id) {
        return findExistingOrThrow(id).toDTO();
    }

    @Override
    public List<InvoiceDTO> findInvoiceByFilter(InvoiceDTO filter, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new InvoiceValidationException("dateFrom no puede ser posterior a dateTo.");
        }

        return invoiceRepository.findByFilter(filter, dateFrom, dateTo).stream()
                .map(InvoiceDomain::toDTO)
                .toList();
    }

    @Override
    public List<InvoiceDTO> findAll() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceDomain::toDTO)
                .toList();
    }

    private InvoiceDomain findExistingOrThrow(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("No existe una factura con id " + id));
    }

    private UUID generateUniqueId() {
        UUID candidate = UUID.randomUUID();
        while (invoiceRepository.existsById(candidate)) {
            candidate = UUID.randomUUID();
        }
        return candidate;
    }

    private String generateNextInvoiceNumber(UUID headquarterId) {
        return invoiceRepository.findLastInvoiceNumberByHeadquarter(headquarterId)
                .map(last -> increment(last.getInvoiceNumber()))
                .orElse(format(1));
    }

    private String increment(String lastInvoiceNumber) {
        String numericPart = lastInvoiceNumber.substring(INVOICE_NUMBER_PREFIX.length());
        int next = Integer.parseInt(numericPart) + 1;
        return format(next);
    }

    private String format(int sequence) {
        return INVOICE_NUMBER_PREFIX + String.format("%0" + INVOICE_NUMBER_DIGITS + "d", sequence);
    }
}
