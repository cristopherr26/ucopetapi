package com.uco.ucopetapi.controllers.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.mapper.OrderMapper;
import com.uco.ucopetapi.dto.order.OrderDTO;
import com.uco.ucopetapi.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAllOrders() {
        List<OrderDTO> orders = orderService.findAll()
                .stream()
                .map(orderMapper::toDTO)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> findOrderById(@PathVariable UUID id) {
        return orderService.findById(id)
                .map(orderMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createNewOrder(@Valid @RequestBody OrderDTO orderDto) {
        OrderDomain savedDomain = orderService.save(orderMapper.toDomain(orderDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDTO(savedDomain));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody OrderDTO orderDto) {

        OrderDomain updatedDomain = orderService.update(id, orderMapper.toDomain(orderDto));
        return ResponseEntity.ok(orderMapper.toDTO(updatedDomain));
    }

    @PatchMapping("/{id}/authorize")
    public ResponseEntity<OrderDTO> authorizeOrder(
            @PathVariable UUID id,
            @RequestParam Boolean isAuthorized) {

        OrderDomain authorizedDomain = orderService.authorize(id, isAuthorized);
        return ResponseEntity.ok(orderMapper.toDTO(authorizedDomain));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}