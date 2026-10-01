package com.uco.ucopetapi.service.person;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
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
import com.uco.ucopetapi.event.LoginSuccededEvent;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.security.UnknownEmailAttempts;
import com.uco.ucopetapi.security.JwtService;

@Service
public class PersonService {
    private static final String INVALID_CREDENTIALS = "Correo o contrasena incorrectos";

    private static final int ATTEMPTS_TO_LOCK = 5;
    private static final int FIRST_LOCK_MINUTES = 15;
    private static final int SECOND_LOCK_MINUTES = 60;

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UnknownEmailAttempts unknownEmailAttempts;
    private final IDoctorRepository doctorRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PersonService(PersonRepository personRepository,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService,
                             UnknownEmailAttempts unknownEmailAttempts,
                          IDoctorRepository doctorRepository, ApplicationEventPublisher eventPublisher) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.unknownEmailAttempts = unknownEmailAttempts;
        this.doctorRepository = doctorRepository;
        this.eventPublisher = eventPublisher;
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
                        "No existe una person con ese documento"));
    }

    @Transactional(readOnly = true)
    public PersonDTO findByEmail(String email) {
        return personRepository.findByEmail(email)
                .map(PersonService::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una person con ese correo"));
    }

    @Transactional
    public PersonDTO create(PersonDTO person) {
        validateUniqueness(person, null);

        PersonDomain created = new PersonDomain();
        copyData(person, created);
        created.setAdmin(person.admin());
        created.setActive(true);

        return toDTO(saveOrConflict(created));
    }

    @Transactional
    public PersonDTO update(UUID personId, PersonDTO person) {
        PersonDomain current = findOrThrow(personId);
        validateUniqueness(person, personId);

        copyData(person, current);
        if (!person.active()) {
            validateNotLastAdmin(current);
        }
        current.setActive(person.active());
        syncDeactivation(current);

        return toDTO(saveOrConflict(current));
    }

    @Transactional
    public void delete(UUID personId) {
        PersonDomain current = findOrThrow(personId);
        validateNotLastAdmin(current);
        current.setActive(false);
        syncDeactivation(current);
        personRepository.save(current);
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
        Optional<PersonDomain> found = personRepository.findByEmail(request.email());
        if (found.isEmpty()) {
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

        if (!person.isActive() || !passwordMatches(request.password(), person.getPasswordHash())) {
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

    private void copyData(PersonDTO desde, PersonDomain hacia) {
        hacia.setDocumentType(desde.documentType());
        hacia.setDocumentNumber(desde.documentNumber());
        hacia.setFirstName(desde.firstName());
        hacia.setLastName(desde.lastName());
        hacia.setEmail(desde.email());
        hacia.setAddress(desde.address());
        hacia.setPhone(desde.phone());
    }

    private void validateNotLastAdmin(PersonDomain person) {
        if (person.isAdmin() && person.isActive()
                && personRepository.countByAdminTrueAndActiveTrue() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede dar de baja al unico administrador activo. "
                            + "Primero hay que darle el rol a otra person.");
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

    private void syncDeactivation(PersonDomain person) {
        if (person.isActive()) {
            person.setDeactivatedAt(null);
        } else if (person.getDeactivatedAt() == null) {
            person.setDeactivatedAt(Instant.now());
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

    private boolean passwordMatches(String raw, String almacenada) {
        return almacenada != null && passwordEncoder.matches(raw, almacenada);
    }

    private void validateUniqueness(PersonDTO person, UUID personId) {
        if (person.email() != null && !person.email().isBlank()) {
            personRepository.findByEmail(person.email())
                    .filter(otra -> !otra.getId().equals(personId))
                    .ifPresent(_ -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ya hay una person registrada con ese correo");
                    });
        }
        personRepository.findByDocumentTypeAndDocumentNumber(
                        person.documentType(), person.documentNumber())
                .filter(otra -> !otra.getId().equals(personId))
                .ifPresent(_ -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya hay una person registrada con ese documento");
                });
    }

    private PersonDomain saveOrConflict(PersonDomain person) {
        try {
            return personRepository.saveAndFlush(person);
        } catch (DataIntegrityViolationException _) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El correo o el documento ya estan registrados");
        }
    }

    private PersonDomain findOrThrow(UUID personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe una person con ese id"));
    }

    private static PersonDTO toDTO(PersonDomain person) {
        return new PersonDTO(person.getId(), person.getDocumentType(),
                person.getDocumentNumber(), person.getFirstName(), person.getLastName(),
                person.getEmail(), person.getAddress(), person.getPhone(),
                person.isAdmin(), person.isActive(), person.getDeactivatedAt());
    }
}
