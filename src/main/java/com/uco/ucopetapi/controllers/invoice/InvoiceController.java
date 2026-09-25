package com.uco.ucopetapi.controllers.invoice;

import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceFilterRequestDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface InvoiceController {

    ResponseEntity<InvoiceDTO> registerNewInvoice(CreateInvoiceRequestDTO request);

    ResponseEntity<InvoiceDTO> registerPayment(UUID id, RegisterPaymentRequestDTO payment);

    ResponseEntity<InvoiceDTO> cancelInvoice(UUID id);

    ResponseEntity<InvoiceDTO> findById(UUID id);

    ResponseEntity<List<InvoiceDTO>> findInvoiceByFilter(InvoiceFilterRequestDTO filterRequest);

    ResponseEntity<List<InvoiceDTO>> findAll();
}
