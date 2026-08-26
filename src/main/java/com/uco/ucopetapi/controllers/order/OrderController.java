package com.uco.ucopetapi.controllers.order;

import com.uco.ucopetapi.dto.order.OrderDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private OrderDTO buildDummyOrder(UUID id) {
        return new OrderDTO(
                id != null ? id : UUID.randomUUID(),
                "ORD-2026-001",
                "Juan Pablo Alzate",
                "Firulais",
                "Consulta General + Vacuna",
                "PENDIENTE",
                LocalDateTime.now(),
                false
        );
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAllOrders() {
        List<OrderDTO> orders = List.of(
                buildDummyOrder(UUID.randomUUID())
        );

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<OrderDTO>> findOrdersByFilter(@RequestParam(required = true) UUID id) {
        return ResponseEntity.ok(List.of(buildDummyOrder(id)));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createNewOrder(@RequestBody OrderDTO order) {
        OrderDTO createdOrder = buildDummyOrder(order.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping
    public ResponseEntity<OrderDTO> updateOrder(
            @RequestParam(required = true) UUID id,
            @RequestBody OrderDTO order) {

        OrderDTO updatedOrder = buildDummyOrder(id);
        updatedOrder.setEstado("ACTUALIZADO");
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/authorize")
    public ResponseEntity<OrderDTO> authorizeOrder(
            @RequestParam(required = true) UUID id,
            @RequestParam(required = true) boolean isAuthorized) {

        OrderDTO authorizedOrder = buildDummyOrder(id);
        authorizedOrder.setAuthorized(isAuthorized);
        authorizedOrder.setEstado(isAuthorized ? "AUTORIZADO" : "RECHAZADO");
        return ResponseEntity.ok(authorizedOrder);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteOrder(@RequestParam(required = true) UUID id) {
        return ResponseEntity.noContent().build();
    }

}
