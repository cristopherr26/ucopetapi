package com.uco.ucopetapi.controllers.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.service.egress.EgressService;
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

    private final EgressService egressService;

    public EgressControler(EgressService egressService) {
        this.egressService = egressService;
    }


    @GetMapping("/Egresses")
    public ResponseEntity<List<EgressDomain>> getAllEgresses() {
        return ResponseEntity.ok(egressService.obtenerTodos());
    }

    @GetMapping("/Egresses/{id}")
    public ResponseEntity<EgressDomain> getEgressById(@PathVariable UUID id) {
        return ResponseEntity.ok(egressService.obtenerPorId(id));
    }

    @PostMapping("/newEgress")
    public ResponseEntity<EgressDomain> createEgress(@RequestBody Map<String, Object> request) {

        EgressDomain nuevoEgress = new EgressDomain(
                null,
                LocalDate.parse((String) request.get("date")),
                (UUID) request.get("provider"),
                (UUID)request.get("payMethod"),
                (UUID) request.get("purchaseOrder"),
                (String) request.get("concept"),
                Float.valueOf(request.get("total").toString())
        );

        EgressDomain guardado = egressService.guardar(nuevoEgress);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EgressDomain> updateEgress(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {

        EgressDomain egressActualizado = new EgressDomain(
                id,
                LocalDate.parse((String) request.get("date")),
                (UUID) request.get("provider"),
                (UUID)request.get("payMethod"),
                (UUID) request.get("purchaseOrder"),
                (String) request.get("concept"),
                Float.valueOf(request.get("total").toString())
        );

        EgressDomain resultado = egressService.actualizar(id, egressActualizado);
        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEgress(@PathVariable UUID id) {
        egressService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
