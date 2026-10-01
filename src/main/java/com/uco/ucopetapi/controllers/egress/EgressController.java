package com.uco.ucopetapi.controllers.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.dto.egress.EgressDTO;
import com.uco.ucopetapi.dto.egress.EgressRequestDTO;
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

    @Value("${paymethod.service.url:http://localhost:8080/api/v1/PayMethods/getIdByName}")
    private String payMethodServiceUrl;

    @Value("${provider.service.url:http://localhost:8080/api/v1/providers/filter}")
    private String providerServiceUrl;

    @Value("${purchaseorder.service.url:http://localhost:8080/api/v1/purchases/lookup}")
    private String purchaseOrderServiceUrl;

    public EgressController(EgressService egressService) {
        this.egressService = egressService;
    }

    @GetMapping()
    public ResponseEntity<List<EgressDTO>> getAllEgresses() {
        return ResponseEntity.ok(egressService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EgressDTO> getEgressById(@PathVariable UUID id) {
        return ResponseEntity.ok(egressService.getById(id));
    }

    @GetMapping("/getByConcept")
    public ResponseEntity<List<EgressDTO>> getByConcept(@RequestParam String concept) {
        return ResponseEntity.ok(egressService.getByConcept(concept));
    }

    @GetMapping("/getByDateRange")
    public ResponseEntity<List<EgressDTO>> getByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        LocalDate start = parseDate(startDate, "startDate");
        LocalDate end = parseDate(endDate, "endDate");
        return ResponseEntity.ok(egressService.getByDateBetween(start, end));
    }

    @GetMapping("/getByProvider")
    public ResponseEntity<List<EgressDTO>> getByProvider(
            @RequestParam String provider,
            HttpServletRequest httpRequest) {

        UUID providerId = getProviderIdByName(provider, authHeader(httpRequest));
        return ResponseEntity.ok(egressService.getByProvider(providerId));
    }

    @GetMapping("/getByPayMethod")
    public ResponseEntity<List<EgressDTO>> getByPayMethod(
            @RequestParam String payMethod,
            HttpServletRequest httpRequest) {

        UUID payMethodId = getPayMethodIdByName(payMethod, authHeader(httpRequest));
        return ResponseEntity.ok(egressService.getByPayMethod(payMethodId));
    }

    @GetMapping("/getByPurchaseOrder")
    public ResponseEntity<List<EgressDTO>> getByPurchaseOrder(
            @RequestParam String purchaseOrder,
            HttpServletRequest httpRequest) {

        UUID purchaseOrderId = getPurchaseOrderIdByNumber(purchaseOrder, authHeader(httpRequest));
        return ResponseEntity.ok(egressService.getByPurchaseOrder(purchaseOrderId));
    }

    @PostMapping("/newEgress")
    public ResponseEntity<EgressDTO> createEgress(
            @RequestBody EgressRequestDTO request,
            HttpServletRequest httpRequest) {

        EgressDomain newEgress = buildEgressFromNames(null, request, authHeader(httpRequest));
        EgressDTO savedEgress = egressService.saveEgress(newEgress);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEgress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EgressDTO> updateEgress(
            @PathVariable UUID id,
            @RequestBody EgressRequestDTO request,
            HttpServletRequest httpRequest) {

        EgressDomain updatedEgress = buildEgressFromNames(id, request, authHeader(httpRequest));
        EgressDTO result = egressService.updateEgress(id, updatedEgress);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEgress(@PathVariable UUID id) {
        egressService.deleteEgress(id);
        return ResponseEntity.noContent().build();
    }

    private EgressDomain buildEgressFromNames(UUID id, EgressRequestDTO request, String authHeader) {
        UUID providerId = getProviderIdByName(request.getProvider(), authHeader);
        UUID purchaseOrderId = getPurchaseOrderIdByNumber(request.getPurchaseOrder(), authHeader);
        UUID payMethodId = getPayMethodIdByName(request.getPayMethod(), authHeader);

        return new EgressDomain(
                id,
                parseDate(request.getDate(), "date"),
                providerId,
                payMethodId,
                purchaseOrderId,
                request.getConcept(),
                request.getTotal()
        );
    }

    private String authHeader(HttpServletRequest httpRequest) {
        return httpRequest.getHeader("Authorization");
    }

    private UUID getProviderIdByName(String providerName, String authHeader) {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("El campo 'provider' es obligatorio");
        }
        try {
            UUID id = restClient.get()
                    .uri(providerServiceUrl + "?providerName={providerName}", providerName)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .body(UUID.class);

            if (id == null) {
                throw new IllegalArgumentException("Proveedor no encontrado: " + providerName);
            }
            return id;
        } catch (HttpClientErrorException.NotFound _) {
            throw new IllegalArgumentException("Proveedor no encontrado: " + providerName);
        }
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