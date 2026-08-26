package com.uco.ucopetapi.controllers.egress;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class EgressControler {

    @GetMapping("/Egresses")
    public ResponseEntity<List<Map<String, Object>>> getAllEgresses() {
        List<Map<String, Object>> egresses = List.of(
                Map.of(
                        "id", UUID.fromString("105bd9b5-9db9-47df-bd1e-b3fb8271420b"),
                        "date", ("12/05/2026"),
                        "provider", UUID.fromString("b905c6cf-e8f9-465a-b409-86e2e4a385a3"),
                        "payMethod", UUID.fromString("4b3fb2db-f63a-41a2-8233-43b73d2c1176"),
                        "product", ("Diclofenaco"),
                        "quantity", ("2"),
                        "price", ("$30.000"),
                        "totalPrice", ("$60.000")
                )
        );
        return ResponseEntity.ok(egresses);
    }

    @PostMapping("/newEgress")
    public ResponseEntity<Map<String, Object>> createEgress(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = Map.of(
                "id", UUID.randomUUID(),
                "date", request.getOrDefault("date", "21/03/2026"),
                "provider", UUID.randomUUID(),
                "payMethod", UUID.randomUUID(),
                "product", request.getOrDefault("product", "Dolex"),
                "quantity", request.getOrDefault("quantity", "21"),
                "price", request.getOrDefault("price", "$5.000"),
                "totalPrice", request.getOrDefault("totalPrice", "105.000")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEgress(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", id,
                "date", request.getOrDefault("date", "21/05/2026"),
                "provider", UUID.randomUUID(),
                "payMethod", UUID.randomUUID(),
                "product", request.getOrDefault("product", "Dolex"),
                "quantity", request.getOrDefault("quantity", "21"),
                "price", request.getOrDefault("price", "$5.000"),
                "totalPrice", request.getOrDefault("totalPrice", "105.000")
        );
        return ResponseEntity.ok(response);
    }
}
