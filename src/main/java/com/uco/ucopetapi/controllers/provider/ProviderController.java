package com.uco.ucopetapi.controllers.provider;

import com.uco.ucopetapi.dto.provider.ProviderDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    // Actividad 5 pendiente: por ahora la respuesta va "quemada" directo aquí.

    private ProviderDTO exampleProvider(UUID id) {
        return new ProviderDTO(
                id,
                "Vetfarma S.A.S.",
                "Laura Gómez",
                "NIT",
                "901234567-1",
                "3006541122",
                "Cra 45 #12-30, Medellín",
                true,
                "contacto@vetfarma.com"
        );
    }

    // El diagrama dice "void", pero devolver el objeto creado (con su id) es
    // el estándar para un POST real — así el cliente sabe qué id se generó.
    @PostMapping
    public ResponseEntity<ProviderDTO> createNewProvider(@Valid @RequestBody ProviderDTO provider) {
        ProviderDTO created = exampleProvider(UUID.randomUUID());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProviderDTO> updateProvider(@PathVariable UUID id,
                                                      @Valid @RequestBody ProviderDTO provider) {
        ProviderDTO updated = exampleProvider(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ProviderDTO> deactivateProvider(@PathVariable UUID id) {
        ProviderDTO deactivated = exampleProvider(id);
        deactivated.setIsActive(false);
        return ResponseEntity.ok(deactivated);
    }

    // El diagrama pide recibir "Providers provider" como filtro, pero un GET
    // no lleva body — se traduce a query params opcionales con el mismo fin.
    @GetMapping("/filter")
    public ResponseEntity<List<ProviderDTO>> findByFilter(@RequestParam(required = false) String idType,
                                                          @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(List.of(exampleProvider(UUID.randomUUID())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(exampleProvider(id));
    }

    @GetMapping
    public ResponseEntity<List<ProviderDTO>> findAll() {
        return ResponseEntity.ok(List.of(exampleProvider(UUID.randomUUID())));
    }
}