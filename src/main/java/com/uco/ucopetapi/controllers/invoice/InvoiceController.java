package com.uco.ucopetapi.controllers.invoice;

import com.uco.ucopetapi.dto.invoice.CreateInvoiceRequestDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceFilterRequestDTO;
import com.uco.ucopetapi.dto.invoice.RegisterPaymentRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InvoiceController {

    ResponseEntity<InvoiceDTO> registerNewInvoice(CreateInvoiceRequestDTO request);

    ResponseEntity<InvoiceDTO> registerPayment(UUID id, RegisterPaymentRequestDTO payment);

    ResponseEntity<InvoiceDTO> cancelInvoice(UUID id);

    ResponseEntity<InvoiceDTO> findById(UUID id);

    ResponseEntity<List<InvoiceDTO>> findInvoiceByFilter(InvoiceFilterRequestDTO filterRequest);

    ResponseEntity<List<InvoiceDTO>> findAll();
}
