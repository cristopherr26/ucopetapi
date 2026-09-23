package com.uco.ucopetapi.service.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.OrderState;
import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.dto.pet.PetDTO;
import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.event.OrderStatusEvent;
import com.uco.ucopetapi.repository.order.IOrderRepository;
import com.uco.ucopetapi.service.order.exception.InvalidOrderRequestException;
import com.uco.ucopetapi.service.order.exception.InvalidOrderStateException;
import com.uco.ucopetapi.service.order.exception.OrderNotFoundException;
import com.uco.ucopetapi.service.pet.PetService;
import com.uco.ucopetapi.service.procedure.ProcedureService;
import com.uco.ucopetapi.service.tutorPet.TutorPetService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    private final PetService petService;
    private final ProcedureService procedureService;
    private final TutorPetService tutorPetService;
    private final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderService(IOrderRepository orderRepository, PetService petService,
                        ProcedureService procedureService, TutorPetService tutorPetService, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.petService = petService;
        this.procedureService = procedureService;
        this.tutorPetService = tutorPetService;
        this.eventPublisher = eventPublisher;
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

        procedureService.findById(order.getProcedureId());
        PetDTO pet = petService.getById(order.getPetId());
        TutorPetDTO tutorPet = tutorPetService.findById(pet.getTutorId());

        order.setTutorId(tutorPet.getPerson());

        return orderRepository.save(order);
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

        procedureService.findById(newProcedureId);
        order.setProcedure(entityManager.getReference(ProcedureDomain.class, newProcedureId));

        return orderRepository.save(order);
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
        return orderSaved;
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        OrderDomain order = findById(id);
        requireStatus(order, OrderState.PENDING, "eliminar");
        orderRepository.delete(order);
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
    }

    private void initializeDefaultValues(OrderDomain order) {
        order.setState(OrderState.PENDING);
        order.setIsAuthorized(false);
        order.setDate(LocalDateTime.now(ZoneId.of("America/Bogota")));
    }
}