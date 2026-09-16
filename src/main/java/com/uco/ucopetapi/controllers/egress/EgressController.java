package com.uco.ucopetapi.controllers.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.service.egress.EgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/Egresses")
public class EgressController {

    private final EgressService egressService;
    private final RestClient restClient = RestClient.create();
    private static final String PAY_METHOD_SERVICE_URL = "http://localhost:8080/api/v1/PayMethods/getIdByName";
    private static final String MESSAGE = "error";

    public EgressController(EgressService egressService) {
        this.egressService = egressService;
    }

    @GetMapping()
    public ResponseEntity<List<EgressDomain>> getAllEgresses() {
        return ResponseEntity.ok(egressService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EgressDomain> getEgressById(@PathVariable UUID id) {
        return ResponseEntity.ok(egressService.getById(id));
    }

    @GetMapping("/getByConcept")
    public ResponseEntity<List<EgressDomain>> getByConcept(@RequestParam String concept) {
        return ResponseEntity.ok(egressService.getByConcept(concept));
    }

    @GetMapping("/getByDateRange")
    public ResponseEntity<List<EgressDomain>> getByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        LocalDate start = parseDate(startDate, "startDate");
        LocalDate end = parseDate(endDate, "endDate");
        return ResponseEntity.ok(egressService.getByDateBetween(start, end));
    }

    @GetMapping("/getByProvider")
    public ResponseEntity<List<EgressDomain>> getByProvider(@RequestParam String provider) {
        UUID providerId = getProviderIdByName(provider);
        return ResponseEntity.ok(egressService.getByProvider(providerId));
    }

    @GetMapping("/getByPayMethod")
    public ResponseEntity<List<EgressDomain>> getByPayMethod(@RequestParam String payMethod) {
        UUID payMethodId = getPayMethodIdByName(payMethod);
        return ResponseEntity.ok(egressService.getByPayMethod(payMethodId));
    }

    @GetMapping("/getByPurchaseOrder")
    public ResponseEntity<List<EgressDomain>> getByPurchaseOrder(@RequestParam String purchaseOrder) {
        UUID purchaseOrderId = getPurchaseOrderIdByName(purchaseOrder);
        return ResponseEntity.ok(egressService.getByPurchaseOrder(purchaseOrderId));
    }

    @PostMapping("/newEgress")
    public ResponseEntity<EgressDomain> createEgress(@RequestBody Map<String, Object> request) {
        EgressDomain newEgress = buildEgressFromNames(null, request);
        EgressDomain savedEgress = egressService.saveEgress(newEgress);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEgress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EgressDomain> updateEgress(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {

        EgressDomain updatedEgress = buildEgressFromNames(id, request);
        EgressDomain result = egressService.updateEgress(id, updatedEgress);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEgress(@PathVariable UUID id) {
        egressService.deleteEgress(id);
        return ResponseEntity.noContent().build();
    }

    private EgressDomain buildEgressFromNames(UUID id, Map<String, Object> request) {
        UUID providerId = getProviderIdByName((String) request.get("provider"));
        UUID purchaseOrderId = getPurchaseOrderIdByName((String) request.get("purchaseOrder"));
        UUID payMethodId = getPayMethodIdByName((String) request.get("payMethod"));

        return new EgressDomain(
                id,
                parseDate((String) request.get("date"), "date"),
                providerId,
                payMethodId,
                purchaseOrderId,
                (String) request.get("concept"),
                parseFloat(request.get("total"), "total")
        );
    }

    private UUID getProviderIdByName(String nombre) {
        // TODO: consumir endpoint del microservicio de Provider (ej. GET /api/v1/Providers/buscarPorNombre?nombre=...)
        // y devolver únicamente el UUID, sin depender del Domain/DTO de ese módulo.
        throw new UnsupportedOperationException("Integración con el microservicio de Provider pendiente");
    }

    private UUID getPurchaseOrderIdByName(String nombre) {
        // TODO: consumir endpoint del microservicio de PurchaseOrder (ej. GET /api/v1/PurchaseOrders/buscarPorNombre?nombre=...)
        // y devolver únicamente el UUID.
        throw new UnsupportedOperationException("Integración con el microservicio de PurchaseOrder pendiente");
    }

    private UUID getPayMethodIdByName(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El campo 'payMethod' es obligatorio");
        }

        try {
            UUID id = restClient.get()
                    .uri(PAY_METHOD_SERVICE_URL + "?name={name}", nombre)
                    .retrieve()
                    .body(UUID.class);

            if (id == null) {
                throw new IllegalArgumentException("Método de pago no encontrado: " + nombre);
            }
            return id;
        } catch (HttpClientErrorException.NotFound _) {
            throw new IllegalArgumentException("Método de pago no encontrado: " + nombre);
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El campo '" + fieldName + "' es obligatorio");
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception _) {
            throw new IllegalArgumentException("El campo '" + fieldName + "' no tiene un formato de fecha válido (yyyy-MM-dd)");
        }
    }

    private Float parseFloat(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("El campo '" + fieldName + "' es obligatorio");
        }
        try {
            return Float.valueOf(value.toString());
        } catch (NumberFormatException _) {
            throw new IllegalArgumentException("El campo '" + fieldName + "' no tiene un formato numérico válido");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, String>> handleNotImplemented(UnsupportedOperationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE, ex.getMessage()));
    }
}