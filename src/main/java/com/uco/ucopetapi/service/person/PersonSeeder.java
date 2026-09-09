package com.uco.ucopetapi.service.person;

import java.security.SecureRandom;
import java.util.Base64;

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
    private static final SecureRandom AZAR = new SecureRandom();

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean habilitado;
    private final String contrasenaConfigurada;
    private final String correo;
    private final String nombre;
    private final String apellido;
    private final String documento;

    public PersonSeeder(PersonRepository personRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${ucopet.seed.enabled:true}") boolean habilitado,
                        @Value("${ucopet.admin.password:}") String contrasenaConfigurada,
                        @Value("${ucopet.admin.email:admin@ucopet.com}") String correo,
                        @Value("${ucopet.admin.first-name:Administrador}") String nombre,
                        @Value("${ucopet.admin.last-name:UcoPet}") String apellido,
                        @Value("${ucopet.admin.document:1000000000}") String documento) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.habilitado = habilitado;
        this.contrasenaConfigurada = contrasenaConfigurada;
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

        boolean generada = contrasenaConfigurada == null || contrasenaConfigurada.isBlank();
        String contrasena = generada ? contrasenaAlAzar() : contrasenaConfigurada;

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
        if (generada) {
            LOG.info("   contrasena: {}", contrasena);
            LOG.info("");
            LOG.info("Se genero al azar y NO se vuelve a mostrar. Anotala.");
            LOG.info("Para elegirla vos, define la variable de entorno UCOPET_ADMIN_PASSWORD.");
            LOG.info("Y para que el admin seas vos y no otro:");
            LOG.info("   UCOPET_ADMIN_EMAIL / UCOPET_ADMIN_FIRST_NAME / UCOPET_ADMIN_LAST_NAME");
        } else {
            LOG.info("   contrasena: la de UCOPET_ADMIN_PASSWORD");
        }
        LOG.info("");
        LOG.info("Con ese usuario se crean los demas: POST /api/v1/persons");
        LOG.info("Es una cuenta generica de arranque: creen la suya y usen esa.");
        LOG.info("=====================================================================");
    }

    private static String contrasenaAlAzar() {
        byte[] bytes = new byte[12];
        AZAR.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
