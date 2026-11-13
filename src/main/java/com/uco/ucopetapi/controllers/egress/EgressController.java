package com.uco.ucopetapi.controllers.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.dto.egress.EgressDTO;
import com.uco.ucopetapi.dto.egress.EgressRequestDTO;
import com.uco.ucopetapi.service.egress.EgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/Egresses")
public class EgressController {

    private static final Logger log = LoggerFactory.getLogger(EgressController.class);
    private static final String MESSAGE = "error";

    private final EgressService egressService;

    public EgressController(EgressService egressService) {
        this.egressService = egressService;
    }


    @GetMapping
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(egressService.getByDateBetween(startDate, endDate));
    }

    @GetMapping("/getByProvider")
    public ResponseEntity<List<EgressDTO>> getByProvider(@RequestParam UUID providerId) {
        return ResponseEntity.ok(egressService.getByProvider(providerId));
    }

    @GetMapping("/getByPayMethod")
    public ResponseEntity<List<EgressDTO>> getByPayMethod(@RequestParam UUID payMethodId) {
        return ResponseEntity.ok(egressService.getByPayMethod(payMethodId));
    }

    @GetMapping("/getByPurchaseOrder")
    public ResponseEntity<List<EgressDTO>> getByPurchaseOrder(@RequestParam UUID purchaseOrderId) {
        return ResponseEntity.ok(egressService.getByPurchaseOrder(purchaseOrderId));
    }

    @PostMapping("/newEgress")
    public ResponseEntity<EgressDTO> createEgress(@RequestBody EgressRequestDTO request) {
        EgressDTO acceptedEgress = egressService.saveEgress(toDomain(null, request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(acceptedEgress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EgressDTO> updateEgress(@PathVariable UUID id,
                                                  @RequestBody EgressRequestDTO request) {
        EgressDTO acceptedEgress = egressService.updateEgress(id, toDomain(id, request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(acceptedEgress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEgress(@PathVariable UUID id) {
        egressService.deleteEgress(id);
        return ResponseEntity.accepted().build();
    }

    private EgressDomain toDomain(UUID id, EgressRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo de la petición es obligatorio");
        }
        return new EgressDomain(
                id,
                request.getDate(),
                request.getProvider(),
                request.getPayMethod(),
                request.getPurchaseOrder(),
                request.getConcept(),
                request.getTotal()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(MESSAGE, "El parámetro '" + ex.getName() + "' no tiene un formato válido"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(MESSAGE, "El parámetro '" + ex.getParameterName() + "' es obligatorio"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(MESSAGE, "El cuerpo de la petición no es válido: revisa los ids (UUID) y la fecha (yyyy-MM-dd)"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, String>> handleBrokerUnavailable(AmqpException ex) {
        log.error("[EGRESS] no se pudo publicar en RabbitMQ", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(MESSAGE, "No fue posible enviar la operación a procesar. Intenta de nuevo más tarde."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(RuntimeException ex) {
        log.error("[EGRESS] error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(MESSAGE, "Ocurrió un error inesperado al procesar el egreso"));
    }
}