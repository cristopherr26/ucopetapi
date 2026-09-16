package com.uco.ucopetapi.service.doctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Pruebas de integracion de DoctorService: corren contra la base de datos real
@SpringBootTest
class DoctorServiceIntegrationTest {

    // Id real de la persona admin sembrada por PersonSeeder al arrancar la app.
    // Se usa en vez de un UUID random para que la relacion idPerson sea valida.
    private static final UUID PERSON_ID_ADMIN = UUID.fromString("65d1a6b3-5f3e-4f36-bcca-4b3baf2fcc56");

    @Autowired
    private DoctorService doctorService;

    // Camino feliz: crea un doctor y luego lo busca por el id que devolvio
    // la creacion, verificando que los datos guardados sean los correctos.
    // @Transactional hace que Spring revierta los cambios al terminar la
    // prueba, asi la base de datos queda limpia despues de correrla.
    @Test
    @Transactional
    void deberiaCrearYEncontrarUnDoctorPorId() {
        DoctorDomain nuevoDoctor = new DoctorDomain(null, PERSON_ID_ADMIN, "TEST-001");

        DoctorDomain doctorCreado = doctorService.createNewDoctor(nuevoDoctor);
        assertNotNull(doctorCreado.getId());

        DoctorDomain doctorEncontrado = doctorService.findById(doctorCreado.getId());
        assertEquals(PERSON_ID_ADMIN, doctorEncontrado.getIdPerson());
        assertEquals("TEST-001", doctorEncontrado.getLicenseNumber());
    }

    // Camino de error: si se busca un id que no existe en la base de datos,
    // DoctorService.findById debe lanzar BusinessException (no una excepcion
    // generica), que es lo que despues traduce el controller a un 404.
    // No lleva @Transactional porque no modifica datos, solo lee.
    @Test
    void deberiaLanzarExcepcionSiElDoctorNoExiste() {
        UUID idInexistente = UUID.randomUUID();

        assertThrows(BusinessException.class, () -> doctorService.findById(idInexistente));
    }
}