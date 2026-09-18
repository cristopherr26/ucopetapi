package com.uco.ucopetapi.config;

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
    private final boolean enabled;
    private final String password;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String documentNumber;

    public PersonSeeder(PersonRepository personRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${ucopet.seed.enabled:true}") boolean enabled,
                        @Value("${ucopet.admin.password:UcopetAdmin2026*}") String password,
                        @Value("${ucopet.admin.email:admin@ucopet.com}") String email,
                        @Value("${ucopet.admin.first-name:Administrador}") String firstName,
                        @Value("${ucopet.admin.last-name:UcoPet}") String lastName,
                        @Value("${ucopet.admin.document:1000000000}") String documentNumber) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
    }

    @Override
    public void run(String... args) {
        if (!enabled || personRepository.countByAdminTrueAndActiveTrue() > 0) {
            return;
        }

        PersonDomain admin = new PersonDomain();
        admin.setDocumentType(DocumentType.CC);
        admin.setDocumentNumber(documentNumber);
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setAdmin(true);
        admin.setActive(true);
        admin.setPasswordHash(passwordEncoder.encode(password));
        personRepository.save(admin);

        LOG.info("=====================================================================");
        LOG.info("No habia ningun administrador activo. Se creo el de arranque:");
        LOG.info("   correo:     {}", email);
        LOG.info("   nombre:     {} {}", firstName, lastName);
        LOG.info("   contrasena: {}", password);
        LOG.info("");
        LOG.info("Es generica y su clave esta en el repositorio: cualquiera que lo lea");
        LOG.info("la sabe. Sirve para arrancar, no como cuenta de trabajo.");
        LOG.info("");
        LOG.info("Que hacer ahora:");
        LOG.info("  1. Entrar:        POST /api/v1/persons/login");
        LOG.info("  2. Crear la suya: POST /api/v1/persons  (pide rol ADMIN)");
        LOG.info("  3. Ponerle clave: PUT  /api/v1/persons/[id]/password");
        LOG.info("  4. Y cambiarle la de esta cuenta, o darla de baja");
        LOG.info("");
        LOG.info("Para elegir esta clave al arrancar: UCOPET_ADMIN_PASSWORD=loQueQuieras");
        LOG.info("Si no queda ningun admin activo, esta cuenta se vuelve a crear sola.");
        LOG.info("=====================================================================");
    }

}
