package com.uco.ucopetapi.controllers.space;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/spaces")
public class SpaceController {

    // 1. GET: Obtener Lista General de Espacios
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllSpaces() {
        List<Map<String, Object>> spaces = List.of(
                Map.of(
                        "id", UUID.fromString("e7b9a2c4-8d1e-4f3b-9a2c-1b2c3d4e5f6a"),
                        "type", "Consultorio",
                        "description", "Consultorio principal felinos",
                        "active", true
                ),
                Map.of(
                        "id", UUID.fromString("f8c0b3d5-9e2f-5a4c-8b3d-2c3d4e5f6a7b"),
                        "type", "Quirófano",
                        "description", "Área de cirugía general",
                        "active", false
                )
        );
        return ResponseEntity.ok(spaces);
    }

    // 2. POST: Crear Nuevo Espacio
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSpace(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = Map.of(
                "id", UUID.randomUUID(),
                "type", request.getOrDefault("type", "Peluquería"),
                "description", request.getOrDefault("description", "Zona de baño y corte para caninos"),
                "active", true
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 3. PUT: Editar Descripción del Espacio
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSpaceDescription(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", id,
                "type", "Consultorio",
                "description", request.getOrDefault("description", "Consultorio actualizado para felinos y animales exóticos"),
                "active", true
        );
        return ResponseEntity.ok(response);
    }

    // 4. PATCH: Desactivar Espacio (Inactivar)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateSpace(@PathVariable UUID id) {
        Map<String, Object> response = Map.of(
                "id", id,
                "type", "Consultorio",
                "description", "Consultorio principal felinos",
                "active", false
        );
        return ResponseEntity.ok(response);
    }

    // 5. PATCH: Reactivar Espacio
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateSpace(@PathVariable UUID id) {
        Map<String, Object> response = Map.of(
                "id", id,
                "type", "Consultorio",
                "description", "Consultorio principal felinos",
                "active", true
        );
        return ResponseEntity.ok(response);
    }
}