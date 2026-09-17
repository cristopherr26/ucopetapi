package com.uco.ucopetapi.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.OrderState;
import com.uco.ucopetapi.domain.pet.PetDomain;
import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.service.order.IOrderService;
import com.uco.ucopetapi.service.order.exception.InvalidOrderRequestException;
import com.uco.ucopetapi.service.order.exception.InvalidOrderStateException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class OrderIntegrationTest {

    private static final UUID EXISTING_PET_ID = UUID.fromString("1532ced0-1bb4-43d0-a63d-a40062826e82");

    private static final UUID EXISTING_PROCEDURE_ID = UUID.fromString("ea5fb7fa-da1a-4153-ba33-fe73c6e9dd4f");

    @Autowired
    private IOrderService orderService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldCreateAnOrderSuccessfully() {
        OrderDomain newOrder = buildBaseOrder();

        OrderDomain savedOrder = orderService.save(newOrder);
        entityManager.flush();

        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getIdOrder());
        assertEquals(OrderState.PENDING, savedOrder.getState());
        assertFalse(savedOrder.getIsAuthorized());
        assertEquals(EXISTING_PET_ID, savedOrder.getPetId());
        assertEquals(EXISTING_PROCEDURE_ID, savedOrder.getProcedureId());
    }

    @Test
    void shouldNotAllowDeletingAnAlreadyAuthorizedOrder() {
        OrderDomain order = orderService.save(buildBaseOrder());
        orderService.processAuthorization(order.getId(), true);

        UUID orderId = order.getId();
        InvalidOrderStateException exception = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.delete(orderId)
        );

        assertTrue(exception.getMessage().contains("PENDIENTE"));
    }

    @Test
    void shouldNotCreateAnOrderWithoutAPet() {
        OrderDomain orderWithoutPet = new OrderDomain();
        orderWithoutPet.setProcedure(
                entityManager.getReference(ProcedureDomain.class, EXISTING_PROCEDURE_ID));

        assertThrows(InvalidOrderRequestException.class,
                () -> orderService.save(orderWithoutPet));
    }

    @Test
    void shouldNotCreateAnOrderWithoutAProcedure() {
        OrderDomain orderWithoutProcedure = new OrderDomain();
        orderWithoutProcedure.setPet(
                entityManager.getReference(PetDomain.class, EXISTING_PET_ID));

        assertThrows(InvalidOrderRequestException.class,
                () -> orderService.save(orderWithoutProcedure));
    }

    private OrderDomain buildBaseOrder() {
        OrderDomain order = new OrderDomain();
        order.setPet(entityManager.getReference(PetDomain.class, EXISTING_PET_ID));
        order.setProcedure(entityManager.getReference(ProcedureDomain.class, EXISTING_PROCEDURE_ID));
        return order;
    }
}