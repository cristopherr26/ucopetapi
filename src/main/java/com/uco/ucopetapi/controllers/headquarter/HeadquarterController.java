package com.uco.ucopetapi.controllers.headquarter;

import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/headquarter")

public class HeadquarterController {

    private List<HeadquarterDTO> getMockHeadquarters() {
        List<HeadquarterDTO> list = new ArrayList<>();

        HeadquarterDTO h1 = new HeadquarterDTO();
        h1.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        h1.setName("patitas");
        h1.setAddress("Carrera 52 # 40-25");
        h1.setIsActive(true);

        HeadquarterDTO h2 = new HeadquarterDTO();
        h2.setId(UUID.fromString("987e6543-e21b-34d5-c654-426614174111"));
        h2.setName("animalitos");
        h2.setAddress("Calle 47 # 50-10");
        h2.setIsActive(true);

        list.add(h1);
        list.add(h2);
        return list;
    }

    @GetMapping
    public ResponseEntity<List<HeadquarterDTO>> findByFilter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(getMockHeadquarters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeadquarterDTO> findById(@PathVariable UUID id) {
        HeadquarterDTO h = new HeadquarterDTO();
        h.setId(id);
        h.setName("patitas");
        h.setAddress("Carrera 52 # 40-25");
        h.setIsActive(true);

        return ResponseEntity.ok(h);
    }

    @PostMapping
    public ResponseEntity<HeadquarterDTO> createNewHeadquarter(@RequestBody HeadquarterDTO headquarterDTO) {
        // Asignamos un ID simulado al objeto que entra por el body
        headquarterDTO.setId(UUID.randomUUID());
        if (headquarterDTO.getIsActive() == null) {
            headquarterDTO.setIsActive(true);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(headquarterDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeadquarterDTO> updateHeadquarter(
            @PathVariable UUID id,
            @RequestBody HeadquarterDTO headquarterDTO) {

        headquarterDTO.setId(id);
        return ResponseEntity.ok(headquarterDTO);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<HeadquarterDTO> deactivateHeadquarter(@PathVariable UUID id) {
        HeadquarterDTO deactivated = new HeadquarterDTO();
        deactivated.setId(id);
        deactivated.setName("Sede Inactivada de Prueba");
        deactivated.setAddress("Calle Ficticia 123");
        deactivated.setIsActive(false); // Refleja el estado inactivo

        return ResponseEntity.ok(deactivated);
    }
}