package com.uco.ucopetapi.service.person;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.person.ChangePasswordRequestDTO;
import com.uco.ucopetapi.dto.person.DocumentType;
import com.uco.ucopetapi.dto.person.LoginRequestDTO;
import com.uco.ucopetapi.dto.person.LoginResponseDTO;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.dto.person.SetPasswordRequestDTO;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.security.JwtService;

@Service
public class PersonServiceImpl implements PersonService {
    private static final String CREDENCIALES_INVALIDAS = "Correo o contrasena incorrectos";

    private static final int INTENTOS_PARA_BLOQUEAR = 5;
    private static final int MINUTOS_PRIMER_BLOQUEO = 15;
    private static final int MINUTOS_SEGUNDO_BLOQUEO = 60;

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final IntentosSinCuenta intentosSinCuenta;

    public PersonServiceImpl(PersonRepository personRepository,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService,
                             IntentosSinCuenta intentosSinCuenta) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.intentosSinCuenta = intentosSinCuenta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonDTO> findAll() {
        return personRepository.findAll().stream().map(PersonServiceImpl::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDTO findById(UUID personId) {
        return toDTO(buscar(personId));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDTO findByDocument(DocumentType documentType, String documentNumber) {
        return personRepository.findByDocumentTypeAndDocumentNumber(documentType, documentNumber)
                .map(PersonServiceImpl::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una persona con ese documento"));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonDTO findByEmail(String email) {
        return personRepository.findByEmail(email)
                .map(PersonServiceImpl::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una persona con ese correo"));
    }

    @Override
    @Transactional
    public PersonDTO create(PersonDTO person) {
        validarUnicidad(person, null);

        PersonDomain nueva = new PersonDomain();
        copiarDatos(person, nueva);
        nueva.setAdmin(person.admin());
        nueva.setActive(true);

        return toDTO(guardar(nueva));
    }

    @Override
    @Transactional
    public PersonDTO update(UUID personId, PersonDTO person) {
        PersonDomain actual = buscar(personId);
        validarUnicidad(person, personId);

        copiarDatos(person, actual);
        if (!person.active()) {
            validarQueNoSeaElUltimoAdmin(actual);
        }
        actual.setActive(person.active());
        sincronizarBaja(actual);

        return toDTO(guardar(actual));
    }

    @Override
    @Transactional
    public void delete(UUID personId) {
        PersonDomain actual = buscar(personId);
        validarQueNoSeaElUltimoAdmin(actual);
        actual.setActive(false);
        sincronizarBaja(actual);
        personRepository.save(actual);
    }

    @Override
    @Transactional
    public void logout(UUID personId) {
        PersonDomain actual = buscar(personId);
        actual.setTokenVersion(actual.getTokenVersion() + 1);
        personRepository.save(actual);
    }

    @Override
    @Transactional
    public void setPassword(UUID personId, SetPasswordRequestDTO request) {
        PersonDomain actual = buscar(personId);
        actual.setPasswordHash(passwordEncoder.encode(request.password()));
        actual.setFailedAttempts(0);
        actual.setLockedUntil(null);
        actual.setTokenVersion(actual.getTokenVersion() + 1);
        personRepository.save(actual);
    }

    @Override
    @Transactional
    public void changeOwnPassword(UUID personId, ChangePasswordRequestDTO request) {
        PersonDomain actual = buscar(personId);

        if (!passwordMatches(request.currentPassword(), actual.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "La contrasena actual no es correcta");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contrasena nueva y su confirmacion no coinciden");
        }
        if (request.newPassword().equals(request.currentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contrasena nueva tiene que ser distinta de la actual");
        }

        actual.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        actual.setFailedAttempts(0);
        actual.setLockedUntil(null);
        actual.setTokenVersion(actual.getTokenVersion() + 1);
        personRepository.save(actual);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<PersonDomain> encontrada = personRepository.findByEmail(request.email());
        if (encontrada.isEmpty()) {
            Instant hasta = intentosSinCuenta.bloqueadoHasta(request.email());
            if (hasta != null) {
                throw new ResponseStatusException(HttpStatus.LOCKED, mensajeDeBloqueo(hasta));
            }
            intentosSinCuenta.registrarFallo(request.email(), INTENTOS_PARA_BLOQUEAR,
                    MINUTOS_PRIMER_BLOQUEO, MINUTOS_SEGUNDO_BLOQUEO);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }

        PersonDomain persona = encontrada.get();
        if (estaBloqueada(persona)) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    mensajeDeBloqueo(persona.getLockedUntil()));
        }

        if (!persona.isActive() || !passwordMatches(request.password(), persona.getPasswordHash())) {
            registrarFallo(persona);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }

        limpiarIntentos(persona);

        List<Role> roles = rolesDe(persona);
        if (roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }

        String nombre = persona.getFirstName() + " " + persona.getLastName();
        return new LoginResponseDTO(
                jwtService.generar(persona.getId(), nombre, roles, persona.getTokenVersion()),
                persona.getId(), nombre, roles);
    }

    private void copiarDatos(PersonDTO desde, PersonDomain hacia) {
        hacia.setDocumentType(desde.documentType());
        hacia.setDocumentNumber(desde.documentNumber());
        hacia.setFirstName(desde.firstName());
        hacia.setLastName(desde.lastName());
        hacia.setEmail(desde.email());
        hacia.setAddress(desde.address());
        hacia.setPhone(desde.phone());
    }

    private void validarQueNoSeaElUltimoAdmin(PersonDomain persona) {
        if (persona.isAdmin() && persona.isActive()
                && personRepository.countByAdminTrueAndActiveTrue() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede dar de baja al unico administrador activo. "
                            + "Primero hay que darle el rol a otra persona.");
        }
    }

    private boolean estaBloqueada(PersonDomain persona) {
        return persona.getLockedUntil() != null
                && persona.getLockedUntil().isAfter(Instant.now());
    }

    private String mensajeDeBloqueo(Instant hasta) {
        long minutos = Math.max(1, Duration.between(Instant.now(), hasta).toMinutes() + 1);
        return "Cuenta bloqueada por intentos fallidos. Volve a intentar en "
                + minutos + " minuto" + (minutos == 1 ? "" : "s") + ".";
    }

    private void registrarFallo(PersonDomain persona) {
        int fallos = persona.getFailedAttempts() + 1;
        persona.setFailedAttempts(fallos);

        if (fallos >= INTENTOS_PARA_BLOQUEAR * 2) {
            persona.setLockedUntil(Instant.now().plus(MINUTOS_SEGUNDO_BLOQUEO, ChronoUnit.MINUTES));
        } else if (fallos >= INTENTOS_PARA_BLOQUEAR) {
            persona.setLockedUntil(Instant.now().plus(MINUTOS_PRIMER_BLOQUEO, ChronoUnit.MINUTES));
        }
        personRepository.save(persona);
    }

    private void limpiarIntentos(PersonDomain persona) {
        if (persona.getFailedAttempts() != 0 || persona.getLockedUntil() != null) {
            persona.setFailedAttempts(0);
            persona.setLockedUntil(null);
            personRepository.save(persona);
        }
    }

    private void sincronizarBaja(PersonDomain persona) {
        if (persona.isActive()) {
            persona.setDeactivatedAt(null);
        } else if (persona.getDeactivatedAt() == null) {
            persona.setDeactivatedAt(Instant.now());
        }
    }

    private List<Role> rolesDe(PersonDomain persona) {
        List<Role> roles = new ArrayList<>();
        if (persona.isAdmin()) {
            roles.add(Role.ADMIN);
        }
        return roles;
    }

    private boolean passwordMatches(String raw, String almacenada) {
        return almacenada != null && passwordEncoder.matches(raw, almacenada);
    }

    private void validarUnicidad(PersonDTO person, UUID personId) {
        if (person.email() != null && !person.email().isBlank()) {
            personRepository.findByEmail(person.email())
                    .filter(otra -> !otra.getId().equals(personId))
                    .ifPresent(_ -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ya hay una persona registrada con ese correo");
                    });
        }
        personRepository.findByDocumentTypeAndDocumentNumber(
                        person.documentType(), person.documentNumber())
                .filter(otra -> !otra.getId().equals(personId))
                .ifPresent(_ -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya hay una persona registrada con ese documento");
                });
    }

    private PersonDomain guardar(PersonDomain persona) {
        try {
            return personRepository.saveAndFlush(persona);
        } catch (DataIntegrityViolationException _) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El correo o el documento ya estan registrados");
        }
    }

    private PersonDomain buscar(UUID personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una persona con ese id"));
    }

    private static PersonDTO toDTO(PersonDomain persona) {
        return new PersonDTO(persona.getId(), persona.getDocumentType(),
                persona.getDocumentNumber(), persona.getFirstName(), persona.getLastName(),
                persona.getEmail(), persona.getAddress(), persona.getPhone(),
                persona.isAdmin(), persona.isActive(), persona.getDeactivatedAt());
    }
}
