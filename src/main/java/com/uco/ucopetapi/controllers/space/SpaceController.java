package com.uco.ucopetapi.controllers.space;

import com.uco.ucopetapi.domain.space.SpaceDomain;
import com.uco.ucopetapi.dto.space.SpaceDTO;
import com.uco.ucopetapi.service.space.SpaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    // 1. GET: Listar toda la lista de espacios
    @GetMapping
    public ResponseEntity<List<SpaceDTO>> getAllSpaces() {
        List<SpaceDomain> domains = spaceService.getAllSpaces();
        List<SpaceDTO> dtos = domains.stream()
                .map(d -> new SpaceDTO(d.getId(), d.getCode(), d.getType(), d.getDescription(), d.getActive()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Buscar por Código (Ej: LAB-401)
    @GetMapping("/code/{code}")
    public ResponseEntity<SpaceDTO> getSpaceByCode(@PathVariable String code) {
        Optional<SpaceDomain> domainOut = spaceService.getSpaceByCode(code);

        if (domainOut.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SpaceDomain d = domainOut.get();
        return ResponseEntity.ok(new SpaceDTO(d.getId(), d.getCode(), d.getType(), d.getDescription(), d.getActive()));
    }

    // Buscar por Tipo (Ej: Laboratorio)
    @GetMapping("/type/{type}")
    public ResponseEntity<List<SpaceDTO>> getSpacesByType(@PathVariable String type) {
        List<SpaceDomain> domains = spaceService.getSpacesByType(type);
        List<SpaceDTO> dtos = domains.stream()
                .map(d -> new SpaceDTO(d.getId(), d.getCode(), d.getType(), d.getDescription(), d.getActive()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Buscar por Estado
    @GetMapping("/status/{active}")
    public ResponseEntity<List<SpaceDTO>> getSpacesByStatus(@PathVariable Boolean active) {
        List<SpaceDomain> domains = spaceService.getSpacesByStatus(active);
        List<SpaceDTO> dtos = domains.stream()
                .map(d -> new SpaceDTO(d.getId(), d.getCode(), d.getType(), d.getDescription(), d.getActive()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 2. POST: Crear
    @PostMapping
    public ResponseEntity<SpaceDTO> createSpace(@RequestBody SpaceDTO spaceDTO) {
        String code = (spaceDTO != null && spaceDTO.getCode() != null) ? spaceDTO.getCode() : "ESP-01";
        String type = (spaceDTO != null && spaceDTO.getType() != null) ? spaceDTO.getType() : "Peluquería";
        String description = (spaceDTO != null && spaceDTO.getDescription() != null) ? spaceDTO.getDescription() : "Zona de baño";
        Boolean active = (spaceDTO != null && spaceDTO.isActive() != null) ? spaceDTO.isActive() : true;

        SpaceDomain domainIn = new SpaceDomain(null, code, type, description, active);
        SpaceDomain domainOut = spaceService.createSpace(domainIn);

        SpaceDTO dtoOut = new SpaceDTO(domainOut.getId(), domainOut.getCode(), domainOut.getType(), domainOut.getDescription(), domainOut.getActive());
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoOut);
    }

    // 3. PUT: Actualizar Descripción
    @PutMapping("/{id}")
    public ResponseEntity<SpaceDTO> updateSpaceDescription(@PathVariable UUID id, @RequestBody SpaceDTO spaceDTO) {
        SpaceDomain domainIn = new SpaceDomain(id, null, null, spaceDTO.getDescription(), null);
        Optional<SpaceDomain> domainOut = spaceService.updateSpace(id, domainIn);

        if (domainOut.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SpaceDomain updated = domainOut.get();
        return ResponseEntity.ok(new SpaceDTO(updated.getId(), updated.getCode(), updated.getType(), updated.getDescription(), updated.getActive()));
    }

    // 4. PATCH: Desactivar
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SpaceDTO> deactivateSpace(@PathVariable UUID id) {
        Optional<SpaceDomain> domainOut = spaceService.changeStatus(id, false);

        if (domainOut.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SpaceDomain updated = domainOut.get();
        return ResponseEntity.ok(new SpaceDTO(updated.getId(), updated.getCode(), updated.getType(), updated.getDescription(), updated.getActive()));
    }

    // 5. PATCH: Activar
    @PatchMapping("/{id}/activate")
    public ResponseEntity<SpaceDTO> activateSpace(@PathVariable UUID id) {
        Optional<SpaceDomain> domainOut = spaceService.changeStatus(id, true);

        if (domainOut.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SpaceDomain updated = domainOut.get();
        return ResponseEntity.ok(new SpaceDTO(updated.getId(), updated.getCode(), updated.getType(), updated.getDescription(), updated.getActive()));
    }
}