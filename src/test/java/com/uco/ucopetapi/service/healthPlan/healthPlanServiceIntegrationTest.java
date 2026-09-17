package com.uco.ucopetapi.service.healthPlan;

import com.uco.ucopetapi.dto.healthPlan.HealthPlanDTO;
import com.uco.ucopetapi.exception.healthPlan.HealthPlanNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.open-in-view=false",
        "UCOPET_JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"

})

// Pruebas de integracion de HealthPlanService: corren contra la base de datos real
class HealthPlanServiceIntegrationTest {

    @Autowired
    private HealthPlanService healthPlanService;

    // Camino feliz: crea un health plan y lo busca por el id que devolvio
    // la creacion. @Transactional revierte los cambios al terminar la prueba.
    @Test
    @Transactional
    void deberiaCrearYEncontrarUnHealthPlanPorId() {
        HealthPlanDTO nuevoPlan = new HealthPlanDTO(
                null,
                "Plan de prueba",
                "Sura",
                "Plan creado por un test de integracion",
                "ACTIVA",
                null
        );

        HealthPlanDTO planCreado = healthPlanService.save(nuevoPlan);
        assertNotNull(planCreado.getId());

        HealthPlanDTO planEncontrado = healthPlanService.findById(planCreado.getId());
        assertEquals("Plan de prueba", planEncontrado.getName());
        assertEquals("Sura", planEncontrado.getInsuranceCompany());
        assertEquals("ACTIVA", planEncontrado.getStatus());
    }

    // Camino de error: buscar un id que no existe debe lanzar
    // HealthPlanNotFoundException (no una excepcion generica).
    @Test
    void deberiaLanzarExcepcionSiElHealthPlanNoExiste() {
        UUID idInexistente = UUID.randomUUID();

        assertThrows(
                HealthPlanNotFoundException.class,
                () -> healthPlanService.findById(idInexistente)
        );
    }

    // Borrado logico: despues de delete(), el plan ya no debe ser encontrable
    // por findById (findByIdAndDeletedFalse lo filtra).
    @Test
    @Transactional
    void deberiaMarcarComoEliminadoYNoEncontrarloDespues() {
        HealthPlanDTO nuevoPlan = new HealthPlanDTO(
                null, "Plan a borrar", "Sura", "desc", "ACTIVA", null
        );
        HealthPlanDTO planCreado = healthPlanService.save(nuevoPlan);

        healthPlanService.delete(planCreado.getId());

        assertThrows(
                HealthPlanNotFoundException.class,
                () -> healthPlanService.findById(planCreado.getId())
        );
    }
}
