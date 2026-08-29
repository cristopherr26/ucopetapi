package com.uco.ucopetapi.controllers.payMethod;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class PayMethodController {

    @GetMapping("/metodosDePago")
    public ResponseEntity<List<Map<String, Object>>> getAllPayMethods() {
        List<Map<String, Object>> payMethods = List.of(
                Map.of(
                        "id", UUID.fromString("e7b9a2c4-8d1e-4f3b-9a2c-1b2c3d4e5f6a"),
                        "name", "Transferencia"
                ),
                Map.of(
                        "id", UUID.fromString("f8c0b3d5-9e2f-5a4c-8b3d-2c3d4e5f6a7b"),
                        "name", "Efectivo"
                ),
                Map.of(
                        "id", UUID.fromString("efa45b6b-9eb1-4a5f-baf1-22d29bc3420f"),
                        "name", "Crédito"
                )
        );
        return ResponseEntity.ok(payMethods);
    }

}
