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

    /** ID de una mascota que debe existir en la base de datos, con un tutor válido asociado. */
    private static final UUID EXISTING_PET_ID = UUID.fromString("1532ced0-1bb4-43d0-a63d-a40062826e82");

    /** ID de un procedimiento que debe existir en la base de datos. */
    private static final UUID EXISTING_PROCEDURE_ID = UUID.fromString("ea5fb7fa-da1a-4153-ba33-fe73c6e9dd4f");

    @Autowired
    private IOrderService orderService;

    /** Se usa para construir referencias livianas a entidades (getReference) sin consultas
     *  adicionales, y para forzar un flush cuando una prueba necesita leer de inmediato
     *  un valor generado por la base de datos. */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Camino feliz: crear una orden con IDs válidos de mascota/procedimiento debe
     * funcionar correctamente y devolver la orden con todos sus valores por
     * defecto bien inicializados.
     */
    @Test
    void shouldCreateAnOrderSuccessfully() {
        OrderDomain newOrder = buildBaseOrder();

        OrderDomain savedOrder = orderService.save(newOrder);
        entityManager.flush(); // fuerza el INSERT real, para que idOrder ya venga poblado

        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getIdOrder());
        assertEquals(OrderState.PENDIENTE, savedOrder.getState());
        assertFalse(savedOrder.getIsAuthorized());
        assertEquals(EXISTING_PET_ID, savedOrder.getPetId());
        assertEquals(EXISTING_PROCEDURE_ID, savedOrder.getProcedureId());
    }

    /**
     * Regla de negocio: una orden solo se puede eliminar mientras esté en
     * estado PENDIENTE. Una vez autorizada, intentar eliminarla debe rechazarse.
     */
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

    /**
     * Regla de validación: una orden no se puede crear sin una mascota,
     * sin importar si el procedimiento es válido o no.
     */
    @Test
    void shouldNotCreateAnOrderWithoutAPet() {
        OrderDomain orderWithoutPet = new OrderDomain();
        orderWithoutPet.setProcedure(
                entityManager.getReference(ProcedureDomain.class, EXISTING_PROCEDURE_ID));

        assertThrows(InvalidOrderRequestException.class,
                () -> orderService.save(orderWithoutPet));
    }

    /**
     * Regla de validación: una orden no se puede crear sin un procedimiento,
     * sin importar si la mascota es válida o no.
     */
    @Test
    void shouldNotCreateAnOrderWithoutAProcedure() {
        OrderDomain orderWithoutProcedure = new OrderDomain();
        orderWithoutProcedure.setPet(
                entityManager.getReference(PetDomain.class, EXISTING_PET_ID));

        assertThrows(InvalidOrderRequestException.class,
                () -> orderService.save(orderWithoutProcedure));
    }

    /**
     * Construye una {@link OrderDomain} mínima y válida, apuntando a la
     * mascota y el procedimiento de prueba ya existentes, lista para
     * pasarle a {@code orderService.save(...)}.
     */
    private OrderDomain buildBaseOrder() {
        OrderDomain order = new OrderDomain();
        order.setPet(entityManager.getReference(PetDomain.class, EXISTING_PET_ID));
        order.setProcedure(entityManager.getReference(ProcedureDomain.class, EXISTING_PROCEDURE_ID));
        return order;
    }
}