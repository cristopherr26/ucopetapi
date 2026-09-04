package com.uco.ucopetapi.controllers.specialtie;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/specialtie")
public class SpecialtieController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllSpecialties() {
        List<Map<String, Object>> spaces = List.of(
                Map.of(
                        "id", "e7b9a2c4-8d1e-4f3b-9a2c-1b2c3d4e5f6a",
                        "name", "Cardiología",
                        "description", "Estudia, diagnostica y trata las enfermedades del corazón y de los vasos sanguíneos.",
                        "isActive", true,
                        "certificate", "Información del certificado correspondiente"
                ),
                Map.of(
                        "id", "e8b9a2c4-8d1e-4f3b-9a2c-1b2c3d4e5f6a",
                        "name", "Pediatría",
                        "description", "Se enfoca en la salud y las enfermedades de los niños, desde el nacimiento hasta la adolescencia.",
                        "isActive", false,
                        "certificate", "Información del certificado correspondiente"
                )
        );
        return ResponseEntity.ok(spaces);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSpecialtie(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = Map.of(
                "id", UUID.randomUUID().toString(),
                "name", request.getOrDefault("name", "Neurología"),
                "description", request.getOrDefault("description", "Especialidad no definida"),
                "isActive", true,
                "certificate", "id del certificado correspondiente"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSpecialtieById(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = Map.of(
                "id", id,
                "name", "Urología",
                "description", "se encarga del estudio, diagnóstico y tratamiento de las enfermedades del aparato urinario en hombres y mujeres.",
                "isActive", true,
                "Certificate", "Información del certificado correspondiente"


        );
        return ResponseEntity.ok(response);
    }

}
