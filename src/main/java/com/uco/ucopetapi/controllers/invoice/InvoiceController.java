package com.uco.ucopetapi.controllers.invoice;

import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceAcceptedDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceFilterRequestDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface InvoiceController {

    ResponseEntity<InvoiceAcceptedDTO> registerNewInvoice(CreateInvoiceRequestDTO request);

    ResponseEntity<InvoiceAcceptedDTO> registerPayment(UUID id, RegisterPaymentRequestDTO payment);

    ResponseEntity<InvoiceAcceptedDTO> cancelInvoice(UUID id);

    ResponseEntity<InvoiceDTO> findById(UUID id);

    ResponseEntity<List<InvoiceDTO>> findInvoiceByFilter(InvoiceFilterRequestDTO filterRequest);

    ResponseEntity<List<InvoiceDTO>> findAll();
}