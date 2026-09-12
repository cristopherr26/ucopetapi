package com.uco.ucopetapi.service.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.repository.order.IOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final IOrderRepository orderRepository;

    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderDomain> findAll() {
        return orderRepository.findAll();
    }

    public Optional<OrderDomain> findById(UUID id) {
        return orderRepository.findById(id);
    }

    public OrderDomain save(OrderDomain order) {
        if (order.getDate() == null) {
            order.setDate(LocalDateTime.now(ZoneId.of("America/Bogota")));
        }
        return orderRepository.save(order);
    }

    public OrderDomain update(UUID id, OrderDomain orderDetails) {
        return orderRepository.findById(id).map(existingOrder -> {
            existingOrder.setIdOrder(orderDetails.getIdOrder());
            existingOrder.setTutor(orderDetails.getTutor());
            existingOrder.setPet(orderDetails.getPet());
            existingOrder.setProcedure(orderDetails.getProcedure());
            existingOrder.setState(orderDetails.getState());
            existingOrder.setDate(orderDetails.getDate());
            existingOrder.setAuthorized(orderDetails.getAuthorized());

            return orderRepository.save(existingOrder);
        }).orElseThrow(() -> new RuntimeException("Orden no encontrada con el ID: " + id));
    }

    public OrderDomain authorize(UUID id, Boolean isAuthorized) {
        return orderRepository.findById(id).map(existingOrder -> {
            boolean isApproved = Boolean.TRUE.equals(isAuthorized);

            existingOrder.setAuthorized(isApproved);
            existingOrder.setState(isApproved ? "AUTORIZADO" : "RECHAZADO");

            return orderRepository.save(existingOrder);
        }).orElseThrow(() -> new RuntimeException("Orden no encontrada con el ID: " + id));
    }

    public void delete(UUID id) {
        orderRepository.deleteById(id);
    }
}