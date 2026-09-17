package com.uco.ucopetapi.controllers.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.service.egress.EgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String MESSAGE = "error";

    @Value("${paymethod.service.url}")
    private String payMethodServiceUrl;

    @Value("${provider.service.url}")
    private String providerServiceUrl;

    @Value("${purchaseorder.service.url}")
    private String purchaseOrderServiceUrl;

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
    public ResponseEntity<List<EgressDomain>> getByProvider(
            @RequestParam String provider,
            HttpServletRequest httpRequest) {

        UUID providerId = getProviderIdByName(provider, authHeader(httpRequest));
        return ResponseEntity.ok(egressService.getByProvider(providerId));
    }

    @GetMapping("/getByPayMethod")
    public ResponseEntity<List<EgressDomain>> getByPayMethod(
            @RequestParam String payMethod,
            HttpServletRequest httpRequest) {

        UUID payMethodId = getPayMethodIdByName(payMethod, authHeader(httpRequest));
        return ResponseEntity.ok(egressService.getByPayMethod(payMethodId));
    }

    @GetMapping("/getByPurchaseOrder")
    public ResponseEntity<List<EgressDomain>> getByPurchaseOrder(
            @RequestParam String purchaseOrder,
            HttpServletRequest httpRequest) {

        UUID purchaseOrderId = getPurchaseOrderIdByNumber(purchaseOrder, authHeader(httpRequest));
        return ResponseEntity.ok(egressService.getByPurchaseOrder(purchaseOrderId));
    }

    @PostMapping("/newEgress")
    public ResponseEntity<EgressDomain> createEgress(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        EgressDomain newEgress = buildEgressFromNames(null, request, authHeader(httpRequest));
        EgressDomain savedEgress = egressService.saveEgress(newEgress);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEgress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EgressDomain> updateEgress(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        EgressDomain updatedEgress = buildEgressFromNames(id, request, authHeader(httpRequest));
        EgressDomain result = egressService.updateEgress(id, updatedEgress);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEgress(@PathVariable UUID id) {
        egressService.deleteEgress(id);
        return ResponseEntity.noContent().build();
    }

    private EgressDomain buildEgressFromNames(UUID id, Map<String, Object> request, String authHeader) {
        UUID providerId = getProviderIdByName((String) request.get("provider"), authHeader);
        UUID purchaseOrderId = getPurchaseOrderIdByNumber((String) request.get("purchaseOrder"), authHeader);
        UUID payMethodId = getPayMethodIdByName((String) request.get("payMethod"), authHeader);

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

    private String authHeader(HttpServletRequest httpRequest) {
        return httpRequest.getHeader("Authorization");
    }

    private UUID getProviderIdByName(String nombre, String authHeader) {
        // TODO: confirmar el query param real cuando exista el endpoint en Provider (ej. ?providerName=...)
        throw new UnsupportedOperationException("Integración con el microservicio de Provider pendiente");
    }

    private record PurchaseIdResponse(UUID id) {}

    private UUID getPurchaseOrderIdByNumber(String purchaseNumber, String authHeader) {
        if (purchaseNumber == null || purchaseNumber.isBlank()) {
            throw new IllegalArgumentException("El campo 'purchaseOrder' es obligatorio");
        }
        try {
            PurchaseIdResponse response = restClient.get()
                    .uri(purchaseOrderServiceUrl + "?purchaseNumber={purchaseNumber}", purchaseNumber)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .body(PurchaseIdResponse.class);

            if (response == null || response.id() == null) {
                throw new IllegalArgumentException("Orden de compra no encontrada: " + purchaseNumber);
            }
            return response.id();
        } catch (HttpClientErrorException.NotFound _) {
            throw new IllegalArgumentException("Orden de compra no encontrada: " + purchaseNumber);
        }
    }

    private UUID getPayMethodIdByName(String nombre, String authHeader) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El campo 'payMethod' es obligatorio");
        }
        try {
            UUID id = restClient.get()
                    .uri(payMethodServiceUrl + "?name={name}", nombre)
                    .header("Authorization", authHeader)
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