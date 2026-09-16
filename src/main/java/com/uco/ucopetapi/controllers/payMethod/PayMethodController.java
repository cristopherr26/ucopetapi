package com.uco.ucopetapi.controllers.payMethod;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.service.payMethod.PayMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/PayMethods")
public class PayMethodController {

    private final PayMethodService payMethodService;

    public PayMethodController(PayMethodService payMethodService) {
        this.payMethodService = payMethodService;
    }

    @GetMapping()
    public ResponseEntity<List<PayMethodDomain>> getAll() {
        return ResponseEntity.ok(payMethodService.getAll());
    }

    @GetMapping("/getIdByName")
    public ResponseEntity<UUID> getIdByName(@RequestParam String name) {
        return ResponseEntity.ok(payMethodService.findByName(name).getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayMethodDomain> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(payMethodService.findById(id));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
}