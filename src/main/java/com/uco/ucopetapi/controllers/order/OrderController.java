package com.uco.ucopetapi.controllers.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.dto.order.OrderDTO;
import com.uco.ucopetapi.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private OrderDTO toDTO(OrderDomain domain) {
        return new OrderDTO(
                domain.getId(),
                domain.getIdOrder(),
                domain.getTutor(),
                domain.getPet(),
                domain.getProcedure(),
                domain.getState(),
                domain.getDate(),
                domain.getAuthorized()
        );
    }

    private OrderDomain toDomain(OrderDTO dto) {
        return new OrderDomain(
                dto.getId(),
                dto.getIdOrder(),
                dto.getTutor(),
                dto.getPet(),
                dto.getProcedure(),
                dto.getState(),
                dto.getDate(),
                dto.getAuthorized()
        );
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAllOrders() {
        List<OrderDTO> orders = orderService.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/filter")
    public ResponseEntity<OrderDTO> findOrdersByFilter(@RequestParam(required = true) UUID id) {
        return orderService.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createNewOrder(@Valid @RequestBody OrderDTO order) {
        OrderDomain savedDomain = orderService.save(toDomain(order));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(savedDomain));
    }

    @PutMapping
    public ResponseEntity<OrderDTO> updateOrder(
            @RequestParam(required = true) UUID id,
            @Valid @RequestBody OrderDTO order) {

        OrderDomain updatedDomain = orderService.update(id, toDomain(order));
        return ResponseEntity.ok(toDTO(updatedDomain));
    }

    @PutMapping("/authorize")
    public ResponseEntity<OrderDTO> authorizeOrder(
            @RequestParam(required = true) UUID id,
            @RequestParam(required = true) Boolean isAuthorized) {

        OrderDomain authorizedDomain = orderService.authorize(id, isAuthorized);
        return ResponseEntity.ok(toDTO(authorizedDomain));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteOrder(@RequestParam(required = true) UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}