package com.uco.ucopetapi.service.invoice;

import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceService {

    InvoiceDTO registerNewInvoice(CreateInvoiceRequestDTO request);

    InvoiceDTO registerPayment(UUID id, RegisterPaymentRequestDTO payment);

    InvoiceDTO cancelInvoice(UUID id);

    InvoiceDTO findById(UUID id);

    List<InvoiceDTO> findInvoiceByFilter(InvoiceDTO filter, LocalDate dateFrom, LocalDate dateTo);

    List<InvoiceDTO> findAll();
}
