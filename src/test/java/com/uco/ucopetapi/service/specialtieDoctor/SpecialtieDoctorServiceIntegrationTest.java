package com.uco.ucopetapi.service.specialtieDoctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.service.doctor.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Prueba de integracion de la relacion Doctor-Especialidad, tambien contra
// la base de datos real.
@SpringBootTest
class SpecialtieDoctorServiceIntegrationTest {

    // Mismo id de admin real usado en DoctorServiceIntegrationTest.
    private static final UUID PERSON_ID_ADMIN = UUID.fromString("65d1a6b3-5f3e-4f36-bcca-4b3baf2fcc56");
    // Id de una especialidad de prueba insertada manualmente en la tabla
    // specialtie para poder probar la relacion sin depender de datos random.
    private static final UUID SPECIALTIE_ID_PRUEBA = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private SpecialtieDoctorService specialtieDoctorService;

    // Crea un doctor real, lo asocia a la especialidad de prueba, y verifica
    // que la relacion quede guardada y se pueda encontrar filtrando por el
    // id del doctor.
    @Test
    @Transactional
    void deberiaCrearYEncontrarUnaRelacionDoctorEspecialidad() {
        DoctorDomain doctor = doctorService.createNewDoctor(new DoctorDomain(null, PERSON_ID_ADMIN, "TEST-REL-001"));

        SpecialtieDoctorDomain relacion = new SpecialtieDoctorDomain(null, doctor, SPECIALTIE_ID_PRUEBA);
        SpecialtieDoctorDomain relacionCreada = specialtieDoctorService.createNewSpecialtieDoctor(relacion);
        assertNotNull(relacionCreada.getId());

        List<SpecialtieDoctorDomain> resultado = specialtieDoctorService.findByFilter(doctor.getId(), null);
        assertEquals(1, resultado.size());
        assertEquals(SPECIALTIE_ID_PRUEBA, resultado.get(0).getIdSpecialtie());
    }
}