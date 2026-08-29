package com.uco.ucopetapi.controllers.space;

import com.uco.ucopetapi.dto.space.SpaceDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/spaces")
public class SpaceController {

    // 1. GET: Obtener Lista General de Espacios
    @GetMapping
    public ResponseEntity<List<SpaceDTO>> getAllSpaces() {
        List<SpaceDTO> spaces = List.of(
                new SpaceDTO(UUID.fromString("e7b9a2c4-8d1e-4f3b-9a2c-1b2c3d4e5f6a"), "Consultorio", "Consultorio principal felinos", true),
                new SpaceDTO(UUID.fromString("f8c0b3d5-9e2f-5a4c-8b3d-2c3d4e5f6a7b"), "Quirófano", "Área de cirugía general", false)
        );
        return ResponseEntity.ok(spaces);
    }

    // 2. POST: Crear Nuevo Espacio
    @PostMapping
    public ResponseEntity<SpaceDTO> createSpace(@RequestBody SpaceDTO spaceDTO) {
        String type = (spaceDTO != null && spaceDTO.getType() != null) ? spaceDTO.getType() : "Peluquería";
        String description = (spaceDTO != null && spaceDTO.getDescription() != null) ? spaceDTO.getDescription() : "Zona de baño y corte para caninos";

        // Regla de Negocio: Todo espacio creado nace activo por defecto (true)
        SpaceDTO createdSpace = new SpaceDTO(
                UUID.randomUUID(),
                type,
                description,
                true
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSpace);
    }

    // 3. PUT: Editar Descripción del Espacio
    @PutMapping("/{id}")
    public ResponseEntity<SpaceDTO> updateSpaceDescription(
            @PathVariable UUID id,
            @RequestBody SpaceDTO spaceDTO) {

        String description = (spaceDTO != null && spaceDTO.getDescription() != null) ? spaceDTO.getDescription() : "Consultorio actualizado para felinos y animales exóticos";

        SpaceDTO updatedSpace = new SpaceDTO(
                id,
                "Consultorio",
                description,
                true
        );
        return ResponseEntity.ok(updatedSpace);
    }

    // 4. PATCH: Desactivar Espacio
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SpaceDTO> deactivateSpace(@PathVariable UUID id) {
        SpaceDTO deactivatedSpace = new SpaceDTO(
                id,
                "Consultorio",
                "Consultorio principal felinos",
                false
        );
        return ResponseEntity.ok(deactivatedSpace);
    }

    // 5. PATCH: Reactivar Espacio
    @PatchMapping("/{id}/activate")
    public ResponseEntity<SpaceDTO> activateSpace(@PathVariable UUID id) {
        SpaceDTO activatedSpace = new SpaceDTO(
                id,
                "Consultorio",
                "Consultorio principal felinos",
                true
        );
        return ResponseEntity.ok(activatedSpace);
    }
}