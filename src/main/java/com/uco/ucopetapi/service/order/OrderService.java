package com.uco.ucopetapi.service.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.OrderState;
import com.uco.ucopetapi.domain.order.mapper.OrderMapper;
import com.uco.ucopetapi.event.OrderStatusEvent;
import com.uco.ucopetapi.messaging.order.OrderCreationSenderBroker;
import com.uco.ucopetapi.repository.order.IOrderRepository;
import com.uco.ucopetapi.service.order.exception.InvalidOrderRequestException;
import com.uco.ucopetapi.service.order.exception.InvalidOrderStateException;
import com.uco.ucopetapi.service.order.exception.OrderNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMapper orderMapper;
    private final OrderCreationSenderBroker creationSender;

    public OrderService(IOrderRepository orderRepository, ApplicationEventPublisher eventPublisher,
                        OrderMapper orderMapper, OrderCreationSenderBroker creationSender) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.orderMapper = orderMapper;
        this.creationSender = creationSender;
    }

    @Override
    public List<OrderDomain> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public OrderDomain findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("No existe una orden con id " + id));
    }

    @Transactional
    @Override
    public OrderDomain save(OrderDomain order) {
        validateRequiredFieldsForCreation(order);

        initializeDefaultValues(order);

        OrderDomain orderSaved = orderRepository.saveAndFlush(order);
        publishCreation(orderSaved);
        return orderSaved;
    }

    @Transactional
    @Override
    public OrderDomain changeProcedure(UUID id, UUID newProcedureId) {
        if (newProcedureId == null) {
            throw new InvalidOrderRequestException("El ID del nuevo procedimiento es obligatorio.");
        }

        OrderDomain order = findById(id);
        requireStatus(order, OrderState.PENDING, "cambiar el procedimiento de");

        if (newProcedureId.equals(order.getProcedureId())) {
            throw new InvalidOrderRequestException("El nuevo procedimiento debe ser diferente al actual.");
        }

        order.setProcedureId(newProcedureId);

        OrderDomain orderSaved = orderRepository.save(order);
        publishCreation(orderSaved);
        return orderSaved;
    }

    @Override
    public OrderDomain processAuthorization(UUID id, boolean isApproved) {
        OrderDomain order = findById(id);
        requireStatus(order, OrderState.PENDING, "procesar la autorización de");

        order.setIsAuthorized(isApproved);
        order.setState(isApproved ? OrderState.AUTHORIZED : OrderState.DENIED);

        OrderDomain orderSaved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusEvent(
                order.getId(),
                order.getTutorId(),
                order.getIsAuthorized()
        ));
        publishCreation(orderSaved);
        return orderSaved;
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        OrderDomain order = findById(id);
        requireStatus(order, OrderState.PENDING, "eliminar");
        orderRepository.delete(order);
    }


    private void publishCreation(OrderDomain order) {
        final String idMessage = UUID.randomUUID().toString();
        creationSender.execute(orderMapper.toDTO(order), idMessage);
    }

    private void requireStatus(OrderDomain order, OrderState required, String accion) {
        if (order.getState() == null || order.getState() != required) {
            throw new InvalidOrderStateException(
                    "Solo se puede " + accion + " una orden en estado " + required +
                            ". Estado actual: " + order.getState()
            );
        }
    }

    private void validateRequiredFieldsForCreation(OrderDomain order) {
        if (order.getPetId() == null) {
            throw new InvalidOrderRequestException("El ID de la mascota es obligatorio para crear la orden.");
        }
        if (order.getProcedureId() == null) {
            throw new InvalidOrderRequestException("El ID del procedimiento es obligatorio para crear la orden.");
        }
        if (order.getTutorId() == null) {
            throw new InvalidOrderRequestException("El ID del tutor es obligatorio para crear la orden.");
        }
    }

    private void initializeDefaultValues(OrderDomain order) {
        order.setState(OrderState.PENDING);
        order.setIsAuthorized(false);
        order.setDate(LocalDateTime.now(ZoneId.of("America/Bogota")));
    }
}