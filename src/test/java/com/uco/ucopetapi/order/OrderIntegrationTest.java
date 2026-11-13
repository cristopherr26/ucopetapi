package com.uco.ucopetapi.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.OrderState;
import com.uco.ucopetapi.messaging.order.OrderCreationSenderBroker;
import com.uco.ucopetapi.service.notification.NotificationService;
import com.uco.ucopetapi.service.order.IOrderService;
import com.uco.ucopetapi.service.order.exception.InvalidOrderRequestException;
import com.uco.ucopetapi.service.order.exception.InvalidOrderStateException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class OrderIntegrationTest {

    @Autowired
    private IOrderService orderService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private OrderCreationSenderBroker orderCreationSenderBroker;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldCreateAnOrderSuccessfully() {
        OrderDomain newOrder = buildBaseOrder();
        UUID petId = newOrder.getPetId();
        UUID procedureId = newOrder.getProcedureId();
        UUID tutorId = newOrder.getTutorId();

        OrderDomain savedOrder = orderService.save(newOrder);
        entityManager.flush();

        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getIdOrder());
        assertEquals(OrderState.PENDING, savedOrder.getState());
        assertFalse(savedOrder.getIsAuthorized());
        assertEquals(petId, savedOrder.getPetId());
        assertEquals(procedureId, savedOrder.getProcedureId());
        assertEquals(tutorId, savedOrder.getTutorId());
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

        assertTrue(exception.getMessage().contains("PENDING"));
    }

    @Test
    void shouldNotCreateAnOrderWithoutAPet() {
        OrderDomain orderWithoutPet = buildBaseOrder();
        orderWithoutPet.setPetId(null);

        assertThrows(InvalidOrderRequestException.class,
                () -> orderService.save(orderWithoutPet));
    }

    @Test
    void shouldNotCreateAnOrderWithoutAProcedure() {
        OrderDomain orderWithoutProcedure = buildBaseOrder();
        orderWithoutProcedure.setProcedureId(null);

        assertThrows(InvalidOrderRequestException.class,
                () -> orderService.save(orderWithoutProcedure));
    }

    private OrderDomain buildBaseOrder() {
        OrderDomain order = new OrderDomain();
        order.setPetId(UUID.randomUUID());
        order.setProcedureId(UUID.randomUUID());
        order.setTutorId(UUID.randomUUID());
        return order;
    }
}
