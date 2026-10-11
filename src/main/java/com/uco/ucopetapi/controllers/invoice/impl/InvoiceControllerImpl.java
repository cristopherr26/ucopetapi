package com.uco.ucopetapi.controllers.invoice.impl;

import com.uco.ucopetapi.controllers.invoice.InvoiceController;
import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceFilterRequestDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;
import com.uco.ucopetapi.service.invoice.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceControllerImpl implements InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceControllerImpl(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    @PostMapping
    public ResponseEntity<InvoiceDTO> registerNewInvoice(@RequestBody CreateInvoiceRequestDTO request) {
        InvoiceDTO created = invoiceService.registerNewInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}/payments")
    public ResponseEntity<InvoiceDTO> registerPayment(@PathVariable UUID id,
                                                       @RequestBody RegisterPaymentRequestDTO payment) {
        return ResponseEntity.ok(invoiceService.registerPayment(id, payment));
    }

    @Override
    @PutMapping("/{id}/cancel")
    public ResponseEntity<InvoiceDTO> cancelInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.cancelInvoice(id));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> findInvoiceByFilter(@ModelAttribute InvoiceFilterRequestDTO filterRequest) {

        InvoiceDTO filter = new InvoiceDTO(
                null,
                filterRequest.getSaleOrderId(),
                filterRequest.getInvoiceNumber(),
                filterRequest.getHeadquarterId(),
                filterRequest.getClientID(),
                filterRequest.getPetId(),
                null,
                0,
                0,
                0,
                null,
                filterRequest.getState()
        );

        return ResponseEntity.ok(invoiceService.findInvoiceByFilter(
                filter, filterRequest.getDateFrom(), filterRequest.getDateTo()));
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<List<InvoiceDTO>> findAll() {
        return ResponseEntity.ok(invoiceService.findAll());
    }
}
