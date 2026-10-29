package com.uco.ucopetapi.service.invoice.impl;

import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.invoice.InvoiceDomain;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceAcceptedDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceCancelMessageDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceCreateMessageDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.InvoicePaymentMessageDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;
import com.uco.ucopetapi.dto.invoice.enums.InvoiceState;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import com.uco.ucopetapi.messaging.invoice.InvoiceCancelSenderBroker;
import com.uco.ucopetapi.messaging.invoice.InvoiceCreationSenderBroker;
import com.uco.ucopetapi.messaging.invoice.InvoicePaymentSenderBroker;
import com.uco.ucopetapi.repository.invoice.InvoiceRepository;
import com.uco.ucopetapi.repository.sale.sale.SaleOrderRepository;
import com.uco.ucopetapi.service.invoice.InvoiceService;
import com.uco.ucopetapi.service.invoice.exception.InvoiceNotFoundException;
import com.uco.ucopetapi.service.invoice.exception.InvoiceStateConflictException;
import com.uco.ucopetapi.service.invoice.exception.InvoiceValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final InvoiceCreationSenderBroker invoiceCreationSenderBroker;
    private final InvoicePaymentSenderBroker invoicePaymentSenderBroker;
    private final InvoiceCancelSenderBroker invoiceCancelSenderBroker;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              SaleOrderRepository saleOrderRepository,
                              InvoiceCreationSenderBroker invoiceCreationSenderBroker,
                              InvoicePaymentSenderBroker invoicePaymentSenderBroker,
                              InvoiceCancelSenderBroker invoiceCancelSenderBroker) {
        this.invoiceRepository = invoiceRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.invoiceCreationSenderBroker = invoiceCreationSenderBroker;
        this.invoicePaymentSenderBroker = invoicePaymentSenderBroker;
        this.invoiceCancelSenderBroker = invoiceCancelSenderBroker;
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceAcceptedDTO registerNewInvoice(CreateInvoiceRequestDTO request) {
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

        UUID invoiceId = UUID.randomUUID();
        String idMessage = UUID.randomUUID().toString();
        invoiceCreationSenderBroker.execute(
                new InvoiceCreateMessageDTO(invoiceId, saleOrder.getId(), request.getObservations()), idMessage);
        return new InvoiceAcceptedDTO(invoiceId, idMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceAcceptedDTO registerPayment(UUID id, RegisterPaymentRequestDTO payment) {
        InvoiceDomain existing = findExistingOrThrow(id);

        if (existing.getState() == InvoiceState.ANULADA) {
            throw new InvoiceStateConflictException("No se puede registrar un pago sobre una factura ANULADA.");
        }
        if (existing.getState() == InvoiceState.PAGADA) {
            throw new InvoiceStateConflictException("La factura " + existing.getInvoiceNumber()
                    + " ya se encuentra completamente pagada.");
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvoiceValidationException("amount debe ser mayor a cero.");
        }
        if (existing.getPaidAmount().add(payment.getAmount()).compareTo(existing.getTotal()) > 0) {
            throw new InvoiceValidationException(
                    "El pago excede el saldo pendiente. Saldo actual: " + existing.getBalance());
        }

        String idMessage = UUID.randomUUID().toString();
        invoicePaymentSenderBroker.execute(new InvoicePaymentMessageDTO(id, payment.getAmount()), idMessage);
        return new InvoiceAcceptedDTO(id, idMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceAcceptedDTO cancelInvoice(UUID id) {
        InvoiceDomain existing = findExistingOrThrow(id);

        if (existing.getState() == InvoiceState.ANULADA) {
            throw new InvoiceStateConflictException("La factura " + existing.getInvoiceNumber()
                    + " ya se encuentra ANULADA.");
        }

        String idMessage = UUID.randomUUID().toString();
        invoiceCancelSenderBroker.execute(new InvoiceCancelMessageDTO(id), idMessage);
        return new InvoiceAcceptedDTO(id, idMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO findById(UUID id) {
        return findExistingOrThrow(id).toDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> findInvoiceByFilter(InvoiceDTO filter, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new InvoiceValidationException("dateFrom no puede ser posterior a dateTo.");
        }

        return invoiceRepository.findByFilter(filter, dateFrom, dateTo).stream()
                .map(InvoiceDomain::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> findAll() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceDomain::toDTO)
                .toList();
    }

    private InvoiceDomain findExistingOrThrow(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException("No existe una factura con id " + id));
    }
}