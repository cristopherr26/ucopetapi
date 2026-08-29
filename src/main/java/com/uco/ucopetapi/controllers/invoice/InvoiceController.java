package com.uco.ucopetapi.controllers.invoice;

import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceItemDTO;
import com.uco.ucopetapi.dto.invoice.InvoiceStatus;
import com.uco.ucopetapi.dto.invoice.InvoiceType;
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

/**
 * Controller de Facturacion (Invoice).
 *
 * IMPORTANTE: mientras no tengamos la base de datos conectada, este controller
 * trabaja con datos "quemados" guardados en memoria (invoicesDB). Esto es solo
 * para poder probar los endpoints en Postman y que se pueda validar
 * el contrato de la API. Cuando se conecte la base de datos real, este mapa en
 * memoria se reemplaza por un Service + Repository con JPA.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final Map<UUID, InvoiceDTO> invoicesDB = new LinkedHashMap<>();

    // IDs fijos de ejemplo para que las pruebas en Postman sean repetibles
    private static final UUID SAMPLE_HEADQUARTER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID SAMPLE_TUTOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SAMPLE_PET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SAMPLE_PROVIDER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public InvoiceController() {
        loadMockData();
    }

    private void loadMockData() {
        // Factura de VENTA, parcialmente pagada
        InvoiceDTO saleInvoice = new InvoiceDTO();
        saleInvoice.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        saleInvoice.setInvoiceNumber("FV-0001");
        saleInvoice.setType(InvoiceType.SALE);
        saleInvoice.setHeadquarterId(SAMPLE_HEADQUARTER_ID);
        saleInvoice.setTutorId(SAMPLE_TUTOR_ID);
        saleInvoice.setPetId(SAMPLE_PET_ID);
        saleInvoice.setIssueDate(LocalDateTime.now().minusDays(5));
        saleInvoice.setDueDate(LocalDate.now().plusDays(10));
        saleInvoice.setTotalAmount(150000.0);
        saleInvoice.setPaidAmount(50000.0);
        saleInvoice.setBalance(100000.0);
        saleInvoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        saleInvoice.setObservations("Consulta y vacunacion");
        List<InvoiceItemDTO> saleItems = new ArrayList<>();
        saleItems.add(new InvoiceItemDTO(UUID.randomUUID(), "Consulta general", 1, 80000.0, 80000.0));
        saleItems.add(new InvoiceItemDTO(UUID.randomUUID(), "Vacuna antirrabica", 1, 70000.0, 70000.0));
        saleInvoice.setItems(saleItems);
        invoicesDB.put(saleInvoice.getId(), saleInvoice);

        // Factura de COMPRA, pendiente
        InvoiceDTO purchaseInvoice = new InvoiceDTO();
        purchaseInvoice.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        purchaseInvoice.setInvoiceNumber("FC-0001");
        purchaseInvoice.setType(InvoiceType.PURCHASE);
        purchaseInvoice.setHeadquarterId(SAMPLE_HEADQUARTER_ID);
        purchaseInvoice.setProviderId(SAMPLE_PROVIDER_ID);
        purchaseInvoice.setIssueDate(LocalDateTime.now().minusDays(2));
        purchaseInvoice.setDueDate(LocalDate.now().plusDays(30));
        purchaseInvoice.setTotalAmount(500000.0);
        purchaseInvoice.setPaidAmount(0.0);
        purchaseInvoice.setBalance(500000.0);
        purchaseInvoice.setStatus(InvoiceStatus.PENDING);
        purchaseInvoice.setObservations("Compra de alimento para mascotas");
        List<InvoiceItemDTO> purchaseItems = new ArrayList<>();
        purchaseItems.add(new InvoiceItemDTO(UUID.randomUUID(), "Alimento para perro 20kg", 10, 50000.0, 500000.0));
        purchaseInvoice.setItems(purchaseItems);
        invoicesDB.put(purchaseInvoice.getId(), purchaseInvoice);

        // Factura de VENTA, ya pagada por completo
        InvoiceDTO paidInvoice = new InvoiceDTO();
        paidInvoice.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        paidInvoice.setInvoiceNumber("FV-0002");
        paidInvoice.setType(InvoiceType.SALE);
        paidInvoice.setHeadquarterId(SAMPLE_HEADQUARTER_ID);
        paidInvoice.setTutorId(SAMPLE_TUTOR_ID);
        paidInvoice.setPetId(SAMPLE_PET_ID);
        paidInvoice.setIssueDate(LocalDateTime.now().minusDays(20));
        paidInvoice.setDueDate(LocalDate.now().minusDays(10));
        paidInvoice.setTotalAmount(60000.0);
        paidInvoice.setPaidAmount(60000.0);
        paidInvoice.setBalance(0.0);
        paidInvoice.setStatus(InvoiceStatus.PAID);
        paidInvoice.setObservations("Corte de unas");
        List<InvoiceItemDTO> paidItems = new ArrayList<>();
        paidItems.add(new InvoiceItemDTO(UUID.randomUUID(), "Corte de unas", 1, 60000.0, 60000.0));
        paidInvoice.setItems(paidItems);
        invoicesDB.put(paidInvoice.getId(), paidInvoice);
    }

    // GET /api/invoices?type=SALE&status=PENDING  -> findAll / findByFilter
    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> findAll(
            @RequestParam(required = false) InvoiceType type,
            @RequestParam(required = false) InvoiceStatus status) {

        List<InvoiceDTO> result = new ArrayList<>();
        for (InvoiceDTO invoice : invoicesDB.values()) {
            boolean matchesType = (type == null) || invoice.getType() == type;
            boolean matchesStatus = (status == null) || invoice.getStatus() == status;
            if (matchesType && matchesStatus) {
                result.add(invoice);
            }
        }
        return ResponseEntity.ok(result);
    }

    // GET /api/invoices/{id} -> findById
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> findById(@PathVariable UUID id) {
        InvoiceDTO invoice = invoicesDB.get(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoice);
    }

    // POST /api/invoices -> createNewInvoice
    @PostMapping
    public ResponseEntity<InvoiceDTO> createNewInvoice(@RequestBody InvoiceDTO invoice) {
        invoice.setId(UUID.randomUUID());
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDateTime.now());
        }
        if (invoice.getPaidAmount() == null) {
            invoice.setPaidAmount(0.0);
        }
        double total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0;
        invoice.setBalance(total - invoice.getPaidAmount());
        invoice.setStatus(calculateStatus(invoice.getBalance(), total));
        if (invoice.getItems() == null) {
            invoice.setItems(new ArrayList<>());
        }
        invoicesDB.put(invoice.getId(), invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    // PUT /api/invoices/{id} -> updateInvoice
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(@PathVariable UUID id, @RequestBody InvoiceDTO invoice) {
        if (!invoicesDB.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        invoice.setId(id);
        invoicesDB.put(id, invoice);
        return ResponseEntity.ok(invoice);
    }

    // PUT /api/invoices/{id}/payments -> registerPayment (pago parcial)
    @PutMapping("/{id}/payments")
    public ResponseEntity<InvoiceDTO> registerPayment(@PathVariable UUID id,
                                                      @RequestBody RegisterPaymentRequestDTO payment) {
        InvoiceDTO invoice = invoicesDB.get(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            return ResponseEntity.badRequest().build();
        }

        double newPaidAmount = invoice.getPaidAmount() + payment.getAmount();
        double newBalance = invoice.getTotalAmount() - newPaidAmount;

        invoice.setPaidAmount(newPaidAmount);
        invoice.setBalance(newBalance);
        invoice.setStatus(calculateStatus(newBalance, invoice.getTotalAmount()));

        return ResponseEntity.ok(invoice);
    }

    // PUT /api/invoices/{id}/cancel -> cancelInvoice
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelInvoice(@PathVariable UUID id) {
        InvoiceDTO invoice = invoicesDB.get(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return ResponseEntity.noContent().build();
    }

    // GET /api/invoices/tutor/{tutorId}/pending -> findPendingInvoicesByTutor
    @GetMapping("/tutor/{tutorId}/pending")
    public ResponseEntity<List<InvoiceDTO>> findPendingInvoicesByTutor(@PathVariable UUID tutorId) {
        List<InvoiceDTO> result = new ArrayList<>();
        for (InvoiceDTO invoice : invoicesDB.values()) {
            boolean isFromTutor = tutorId.equals(invoice.getTutorId());
            boolean isPending = invoice.getStatus() == InvoiceStatus.PENDING
                    || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID;
            if (isFromTutor && isPending) {
                result.add(invoice);
            }
        }
        return ResponseEntity.ok(result);
    }

    // GET /api/invoices/provider/{providerId}/pending -> findPendingInvoicesByProvider
    @GetMapping("/provider/{providerId}/pending")
    public ResponseEntity<List<InvoiceDTO>> findPendingInvoicesByProvider(@PathVariable UUID providerId) {
        List<InvoiceDTO> result = new ArrayList<>();
        for (InvoiceDTO invoice : invoicesDB.values()) {
            boolean isFromProvider = providerId.equals(invoice.getProviderId());
            boolean isPending = invoice.getStatus() == InvoiceStatus.PENDING
                    || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID;
            if (isFromProvider && isPending) {
                result.add(invoice);
            }
        }
        return ResponseEntity.ok(result);
    }

    // GET /api/invoices/{id}/items -> findItemsByInvoice ("Ver Items")
    @GetMapping("/{id}/items")
    public ResponseEntity<List<InvoiceItemDTO>> findItemsByInvoice(@PathVariable UUID id) {
        InvoiceDTO invoice = invoicesDB.get(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoice.getItems());
    }

    // GET /api/invoices/{id}/pdf -> generateInvoicePdf
    // TODO: por ahora solo confirma que la factura existe; la generacion real del PDF se hace despues.
    @GetMapping("/{id}/pdf")
    public ResponseEntity<String> generateInvoicePdf(@PathVariable UUID id) {
        InvoiceDTO invoice = invoicesDB.get(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("PDF pendiente de implementar para la factura " + invoice.getInvoiceNumber());
    }

    private InvoiceStatus calculateStatus(double balance, double totalAmount) {
        if (balance <= 0) {
            return InvoiceStatus.PAID;
        } else if (totalAmount > 0 && balance < totalAmount) {
            return InvoiceStatus.PARTIALLY_PAID;
        } else {
            return InvoiceStatus.PENDING;
        }
    }
}
