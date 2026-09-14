package com.uco.ucopetapi.controllers.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.mapper.OrderMapper;
import com.uco.ucopetapi.dto.order.OrderDTO;
import com.uco.ucopetapi.service.order.IOrderService; // <-- Importas la interfaz
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final IOrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(IOrderService orderService, OrderMapper orderMapper) {
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
        OrderDomain order = orderService.findById(id);
        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createNewOrder(@RequestBody OrderDTO orderDto) {
        OrderDomain savedDomain = orderService.save(orderMapper.toDomain(orderDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDTO(savedDomain));
    }

    @PatchMapping("/{id}/procedure")
    public ResponseEntity<OrderDTO> changeProcedure(
            @PathVariable UUID id,
            @RequestBody OrderDTO orderDto) {

        OrderDomain updatedDomain = orderService.changeProcedure(id, orderDto.getProcedureId());
        return ResponseEntity.ok(orderMapper.toDTO(updatedDomain));
    }

    @PatchMapping("/{id}/authorize")
    public ResponseEntity<OrderDTO> authorizeOrder(
            @PathVariable UUID id,
            @RequestBody OrderDTO orderDto) {

        OrderDomain authorizedDomain = orderService.processAuthorization(id, orderDto.getIsAuthorized());
        return ResponseEntity.ok(orderMapper.toDTO(authorizedDomain));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}