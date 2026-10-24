package com.uco.ucopetapi.service.person;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
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
import com.uco.ucopetapi.dto.person.PersonMessageDTO;
import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.dto.person.SetPasswordRequestDTO;
import com.uco.ucopetapi.event.LoginSuccededEvent;
import com.uco.ucopetapi.messaging.person.PersonCreationSenderBroker;
import com.uco.ucopetapi.messaging.person.PersonDeactivationSenderBroker;
import com.uco.ucopetapi.messaging.person.PersonUpdateSenderBroker;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.security.UnknownEmailAttempts;
import com.uco.ucopetapi.security.JwtService;

// Las consultas, el login, el logout y las contrasenas siguen siendo sincronos.
@Service
public class PersonService {
    private static final String INVALID_CREDENTIALS = "Correo o contrasena incorrectos";

    private static final int ATTEMPTS_TO_LOCK = 5;
    private static final int FIRST_LOCK_MINUTES = 15;
    private static final int SECOND_LOCK_MINUTES = 60;
    private static final String DUMMY_PASSWORD = "ucopet-clave-de-relleno-para-tiempos";

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UnknownEmailAttempts unknownEmailAttempts;
    private final IDoctorRepository doctorRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PersonCreationSenderBroker personCreationSenderBroker;
    private final PersonUpdateSenderBroker personUpdateSenderBroker;
    private final PersonDeactivationSenderBroker personDeactivationSenderBroker;
    private final String dummyHash;

    public PersonService(PersonRepository personRepository,
                         PasswordEncoder passwordEncoder,
                         JwtService jwtService,
                         UnknownEmailAttempts unknownEmailAttempts,
                         IDoctorRepository doctorRepository,
                         ApplicationEventPublisher eventPublisher,
                         PersonCreationSenderBroker personCreationSenderBroker,
                         PersonUpdateSenderBroker personUpdateSenderBroker,
                         PersonDeactivationSenderBroker personDeactivationSenderBroker) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.unknownEmailAttempts = unknownEmailAttempts;
        this.doctorRepository = doctorRepository;
        this.eventPublisher = eventPublisher;
        this.personCreationSenderBroker = personCreationSenderBroker;
        this.personUpdateSenderBroker = personUpdateSenderBroker;
        this.personDeactivationSenderBroker = personDeactivationSenderBroker;
        this.dummyHash = passwordEncoder.encode(DUMMY_PASSWORD);
    }

    @Transactional(readOnly = true)
    public List<PersonDTO> findAll() {
        return personRepository.findAll().stream().map(PersonService::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PersonDTO findById(UUID personId) {
        return toDTO(findOrThrow(personId));
    }

    @Transactional(readOnly = true)
    public PersonDTO findByDocument(DocumentType documentType, String documentNumber) {
        return personRepository.findByDocumentTypeAndDocumentNumber(documentType, documentNumber)
                .map(PersonService::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una persona con ese documento"));
    }

    @Transactional(readOnly = true)
    public PersonDTO findByEmail(String email) {
        return Optional.ofNullable(normalizeEmail(email))
                .flatMap(personRepository::findByEmailIgnoreCase)
                .map(PersonService::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una persona con ese correo"));
    }

    @Transactional(readOnly = true)
    public PersonDTO create(PersonDTO person) {
        validateUniqueness(person, null);

        PersonMessageDTO message = toMessage(UUID.randomUUID(), person,
                Boolean.TRUE.equals(person.admin()), true);
        personCreationSenderBroker.execute(message, UUID.randomUUID().toString());

        return new PersonDTO(message.id(), message.documentType(), message.documentNumber(),
                message.firstName(), message.lastName(), message.email(), message.address(),
                message.phone(), message.admin(), message.active(), null);
    }

    @Transactional
    public PersonDTO update(UUID personId, PersonDTO person) {
        return update(personId, person, true);
    }

    @Transactional(readOnly = true)
    public PersonDTO update(UUID personId, PersonDTO person, boolean requestedByAdmin) {
        PersonDomain current = findOrThrow(personId);
        validateUniqueness(person, personId);

        Boolean admin = requestedByAdmin ? person.admin() : null;
        Boolean active = requestedByAdmin ? person.active() : null;

        boolean finalAdmin = admin != null ? admin : current.isAdmin();
        boolean finalActive = active != null ? active : current.isActive();
        if (!finalAdmin || !finalActive) {
            validateNotLastAdmin(current);
        }

        PersonMessageDTO message = toMessage(personId, person, admin, active);
        personUpdateSenderBroker.execute(message, UUID.randomUUID().toString());

        Instant deactivatedAt = finalActive ? null
                : Optional.ofNullable(current.getDeactivatedAt()).orElseGet(Instant::now);
        return new PersonDTO(personId, message.documentType(), message.documentNumber(),
                message.firstName(), message.lastName(), message.email(), message.address(),
                message.phone(), finalAdmin, finalActive, deactivatedAt);
    }

    @Transactional(readOnly = true)
    public void delete(UUID personId) {
        PersonDomain current = findOrThrow(personId);
        validateNotLastAdmin(current);
        personDeactivationSenderBroker.execute(PersonMessageDTO.onlyId(personId),
                UUID.randomUUID().toString());
    }

    @Transactional
    public void logout(UUID personId) {
        PersonDomain current = findOrThrow(personId);
        current.setTokenVersion(current.getTokenVersion() + 1);
        personRepository.save(current);
    }

    @Transactional
    public void setPassword(UUID personId, SetPasswordRequestDTO request) {
        PersonDomain current = findOrThrow(personId);
        current.setPasswordHash(passwordEncoder.encode(request.password()));
        current.setFailedAttempts(0);
        current.setLockedUntil(null);
        current.setTokenVersion(current.getTokenVersion() + 1);
        personRepository.save(current);
    }

    @Transactional
    public void changeOwnPassword(UUID personId, ChangePasswordRequestDTO request) {
        PersonDomain current = findOrThrow(personId);

        if (!passwordMatches(request.currentPassword(), current.getPasswordHash())) {
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

        current.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        current.setFailedAttempts(0);
        current.setLockedUntil(null);
        current.setTokenVersion(current.getTokenVersion() + 1);
        personRepository.save(current);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<PersonDomain> found = personRepository.findByEmailIgnoreCase(normalizeEmail(request.email()));
        if (found.isEmpty()) {
            passwordEncoder.matches(request.password(), dummyHash);
            Instant until = unknownEmailAttempts.lockedUntil(request.email());
            if (until != null) {
                throw new ResponseStatusException(HttpStatus.LOCKED, lockMessage(until));
            }
            Instant lock = lockFor(unknownEmailAttempts.addFailure(request.email()));
            if (lock != null) {
                unknownEmailAttempts.lock(request.email(), lock);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        PersonDomain person = found.get();
        if (isLocked(person)) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    lockMessage(person.getLockedUntil()));
        }

        boolean passwordOk = passwordMatches(request.password(), person.getPasswordHash());
        if (!person.isActive() || !passwordOk) {
            registerFailure(person);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        clearAttempts(person);

        List<Role> roles = rolesOf(person);
        if (roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        String fullName = person.getFirstName() + " " + person.getLastName();

        LoginResponseDTO dto =  new LoginResponseDTO(
                jwtService.generate(person.getId(), fullName, roles, person.getTokenVersion()),
                person.getId(), fullName, roles);

        eventPublisher.publishEvent(new LoginSuccededEvent(
                UUID.randomUUID(),
                person.getId(),
                ZonedDateTime.now(ZoneId.of("America/Bogota"))
        ));
        return dto;
    }

    private static PersonMessageDTO toMessage(UUID id, PersonDTO person, Boolean admin, Boolean active) {
        return new PersonMessageDTO(id, person.documentType(), person.documentNumber(),
                person.firstName(), person.lastName(), normalizeEmail(person.email()),
                person.address(), person.phone(), admin, active);
    }

    private void validateNotLastAdmin(PersonDomain person) {
        if (person.isAdmin() && person.isActive()
                && personRepository.countByAdminTrueAndActiveTrue() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede dejar el sistema sin administradores activos. "
                            + "Primero hay que darle el rol ADMIN a otra persona.");
        }
    }

    private boolean isLocked(PersonDomain person) {
        return person.getLockedUntil() != null
                && person.getLockedUntil().isAfter(Instant.now());
    }

    private String lockMessage(Instant until) {
        long minutes = Math.max(1, Duration.between(Instant.now(), until).toMinutes() + 1);
        return "Cuenta bloqueada por intentos fallidos. Volve a intentar en "
                + minutes + " minuto" + (minutes == 1 ? "" : "s") + ".";
    }

    private static Instant lockFor(int failures) {
        if (failures >= ATTEMPTS_TO_LOCK * 2) {
            return Instant.now().plus(SECOND_LOCK_MINUTES, ChronoUnit.MINUTES);
        }
        if (failures >= ATTEMPTS_TO_LOCK) {
            return Instant.now().plus(FIRST_LOCK_MINUTES, ChronoUnit.MINUTES);
        }
        return null;
    }

    private void registerFailure(PersonDomain person) {
        int failures = person.getFailedAttempts() + 1;
        person.setFailedAttempts(failures);

        Instant until = lockFor(failures);
        if (until != null) {
            person.setLockedUntil(until);
        }
        personRepository.save(person);
    }

    private void clearAttempts(PersonDomain person) {
        if (person.getFailedAttempts() != 0 || person.getLockedUntil() != null) {
            person.setFailedAttempts(0);
            person.setLockedUntil(null);
            personRepository.save(person);
        }
    }

    private List<Role> rolesOf(PersonDomain person) {
        List<Role> roles = new ArrayList<>();
        if (person.isAdmin()) {
            roles.add(Role.ADMIN);
        }
        if (!doctorRepository.findByIdPerson(person.getId()).isEmpty()) {
            roles.add(Role.DOCTOR);
        }
        return roles;
    }

    private boolean passwordMatches(String raw, String stored) {
        if (stored == null) {
            passwordEncoder.matches(raw, dummyHash);
            return false;
        }
        return passwordEncoder.matches(raw, stored);
    }

    private void validateUniqueness(PersonDTO person, UUID personId) {
        String email = normalizeEmail(person.email());
        if (email != null) {
            personRepository.findByEmailIgnoreCase(email)
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

    private PersonDomain findOrThrow(UUID personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una persona con ese id"));
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static PersonDTO toDTO(PersonDomain person) {
        return new PersonDTO(person.getId(), person.getDocumentType(),
                person.getDocumentNumber(), person.getFirstName(), person.getLastName(),
                person.getEmail(), person.getAddress(), person.getPhone(),
                person.isAdmin(), person.isActive(), person.getDeactivatedAt());
    }
}