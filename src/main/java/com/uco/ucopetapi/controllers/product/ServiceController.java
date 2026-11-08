package com.uco.ucopetapi.controllers.product;

import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.service.product.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceDTO> list(@RequestParam(required = false) ServiceCategory category,
                                 @RequestParam(required = false) Boolean active,
                                 @RequestParam(required = false) Boolean purchasable,
                                 @RequestParam(required = false) Boolean sellable,
                                 @RequestParam(required = false) TaxCategory taxCategory,
                                 @RequestParam(required = false) UUID headquarterId) {
        return serviceService.list(category, active, purchasable, sellable, taxCategory, headquarterId);
    }

    @GetMapping("/count-active")
    public long countActiveServices() {
        return serviceService.countActiveServices();
    }

    @GetMapping("/{id}/active")
    public boolean isActive(@PathVariable UUID id) {
        return serviceService.isActive(id);
    }

    @GetMapping("/{id}")
    public ServiceDTO getById(@PathVariable UUID id) {
        return serviceService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ServiceDTO> create(@Valid @RequestBody ServiceDTO service) {
        return new ResponseEntity<>(serviceService.create(service), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> update(@PathVariable UUID id, @Valid @RequestBody ServiceDTO service) {
        return new ResponseEntity<>(serviceService.update(id, service), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        serviceService.deactivate(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
