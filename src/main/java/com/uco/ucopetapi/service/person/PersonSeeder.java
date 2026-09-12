package com.uco.ucopetapi.service.person;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.person.DocumentType;
import com.uco.ucopetapi.repository.person.PersonRepository;

@Component
public class PersonSeeder implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PersonSeeder.class);
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean habilitado;
    private final String contrasena;
    private final String correo;
    private final String nombre;
    private final String apellido;
    private final String documento;

    public PersonSeeder(PersonRepository personRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${ucopet.seed.enabled:true}") boolean habilitado,
                        @Value("${ucopet.admin.password:UcopetAdmin2026*}") String contrasena,
                        @Value("${ucopet.admin.email:admin@ucopet.com}") String correo,
                        @Value("${ucopet.admin.first-name:Administrador}") String nombre,
                        @Value("${ucopet.admin.last-name:UcoPet}") String apellido,
                        @Value("${ucopet.admin.document:1000000000}") String documento) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.habilitado = habilitado;
        this.contrasena = contrasena;
        this.correo = correo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
    }

    @Override
    public void run(String... args) {
        if (!habilitado || personRepository.countByAdminTrueAndActiveTrue() > 0) {
            return;
        }

        PersonDomain admin = new PersonDomain();
        admin.setDocumentType(DocumentType.CC);
        admin.setDocumentNumber(documento);
        admin.setFirstName(nombre);
        admin.setLastName(apellido);
        admin.setEmail(correo);
        admin.setAdmin(true);
        admin.setActive(true);
        admin.setPasswordHash(passwordEncoder.encode(contrasena));
        personRepository.save(admin);

        LOG.info("=====================================================================");
        LOG.info("No habia ningun administrador activo. Se creo el de arranque:");
        LOG.info("   correo:     {}", correo);
        LOG.info("   nombre:     {} {}", nombre, apellido);
        LOG.info("   contrasena: {}", contrasena);
        LOG.info("");
        LOG.info("Es una cuenta de arranque con clave conocida: cualquiera que lea el");
        LOG.info("repositorio la sabe. Entren con ella, creen su propio usuario y");
        LOG.info("cambienle la contrasena a esta: PUT /api/v1/persons/{id}/password");
        LOG.info("Para elegirla al arrancar: UCOPET_ADMIN_PASSWORD=loQueQuieras");
        LOG.info("=====================================================================");
    }

}
