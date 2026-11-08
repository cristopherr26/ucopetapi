package com.uco.ucopetapi.service.invoice;

import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceAcceptedDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    // Escrituras: se validan y se publican en RabbitMQ (las procesa ucopet-payment-processor).
    InvoiceAcceptedDTO registerNewInvoice(CreateInvoiceRequestDTO request);

    InvoiceAcceptedDTO registerPayment(UUID id, RegisterPaymentRequestDTO payment);

    InvoiceAcceptedDTO cancelInvoice(UUID id);

    // Consultas: van directo al repositorio, como antes.
    InvoiceDTO findById(UUID id);

    List<InvoiceDTO> findInvoiceByFilter(InvoiceDTO filter, LocalDate dateFrom, LocalDate dateTo);

    List<InvoiceDTO> findAll();
}