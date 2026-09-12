package com.uco.ucopetapi.controllers.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
//import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.repository.payMethod.PayMethodRepository;
//import com.uco.ucopetapi.repository.provider.ProviderRepository;
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
    //private final ProviderRepository providerRepository;
    private final PayMethodRepository payMethodRepository;

    public EgressControler(EgressService egressService,
                          // ProviderRepository providerRepository,
                           PayMethodRepository payMethodRepository) {
        this.egressService = egressService;
        //this.providerRepository = providerRepository;
        this.payMethodRepository = payMethodRepository;
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

        /*ProviderDomain provider = providerRepository.findById(
                UUID.fromString((String) request.get("providerId"))
        ).orElseThrow(() -> new RuntimeException("Provider no encontrado"));*/

        PayMethodDomain payMethod = payMethodRepository.findById(
                UUID.fromString((String) request.get("payMethodId"))
        ).orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        EgressDomain nuevoEgress = new EgressDomain(
                null,
                LocalDate.parse((String) request.get("date")),
                //provider,
                payMethod,
                (String) request.get("product"),
                (Integer) request.get("quantity"),
                Float.valueOf(request.get("price").toString()),
                Float.valueOf(request.get("totalPrice").toString())
        );

        EgressDomain guardado = egressService.guardar(nuevoEgress);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EgressDomain> updateEgress(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {

        /*ProviderDomain provider = providerRepository.findById(
                UUID.fromString((String) request.get("providerId"))
        ).orElseThrow(() -> new RuntimeException("Provider no encontrado"));*/

        PayMethodDomain payMethod = payMethodRepository.findById(
                UUID.fromString((String) request.get("payMethodId"))
        ).orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        EgressDomain egressActualizado = new EgressDomain(
                id,
                LocalDate.parse((String) request.get("date")),
               // provider,
                payMethod,
                (String) request.get("product"),
                (Integer) request.get("quantity"),
                Float.valueOf(request.get("price").toString()),
                Float.valueOf(request.get("totalPrice").toString())
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
