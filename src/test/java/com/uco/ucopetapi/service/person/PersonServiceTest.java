package com.uco.ucopetapi.service.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.person.ChangePasswordRequestDTO;
import com.uco.ucopetapi.dto.person.DocumentType;
import com.uco.ucopetapi.dto.person.LoginRequestDTO;
import com.uco.ucopetapi.dto.person.LoginResponseDTO;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.event.LoginSuccededEvent;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.security.JwtService;
import com.uco.ucopetapi.security.UnknownEmailAttempts;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonService (unitario, sin base de datos)")
class PersonServiceTest {

    private static final String PASSWORD = "claveCorrecta1";
    private static final String EMAIL = "ana@ucopet.com";

    @Mock
    private PersonRepository personRepository;

    @Mock
    private IDoctorRepository doctorRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    // BCrypt real pero con fuerza 4 (el minimo) para que los tests sean rapidos.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private PersonService service;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService("clave-de-pruebas-de-al-menos-32-caracteres", 60);
        service = new PersonService(personRepository, passwordEncoder, jwtService,
                new UnknownEmailAttempts(), doctorRepository, eventPublisher);
    }

    @Nested
    @DisplayName("Editar una persona")
    class Update {

        @Test
        @DisplayName("si el JSON no trae 'active', la persona sigue activa")
        void missingActiveDoesNotDeactivate() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));
            saveReturnsArgument();

            PersonDTO result = service.update(ana.getId(), dto(ana, null, null), true);

            assertThat(result.active()).isTrue();
            assertThat(ana.getDeactivatedAt()).isNull();
        }

        @Test
        @DisplayName("quien no es ADMIN no se asciende ni se da de baja editando su ficha")
        void nonAdminCannotChangeRoleOrState() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));
            saveReturnsArgument();

            PersonDTO result = service.update(ana.getId(), dto(ana, true, false), false);

            assertThat(result.admin()).isFalse();
            assertThat(result.active()).isTrue();
        }

        @Test
        @DisplayName("el ADMIN puede dar el rol ADMIN, y eso invalida los tokens de esa persona")
        void adminCanGrantAdminRole() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));
            saveReturnsArgument();

            PersonDTO result = service.update(ana.getId(), dto(ana, true, true), true);

            assertThat(result.admin()).isTrue();
            assertThat(ana.getTokenVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("no se le puede quitar el rol al unico ADMIN activo")
        void cannotRevokeLastAdmin() {
            PersonDomain admin = person(true, true, null);
            when(personRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(personRepository.countByAdminTrueAndActiveTrue()).thenReturn(1L);

            assertStatus(() -> service.update(admin.getId(), dto(admin, false, true), true),
                    HttpStatus.CONFLICT);
            verify(personRepository, never()).saveAndFlush(any(PersonDomain.class));
        }

        @Test
        @DisplayName("el ADMIN puede dar de baja por PUT y deactivatedAt se llena solo")
        void adminCanDeactivate() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));
            saveReturnsArgument();

            PersonDTO result = service.update(ana.getId(), dto(ana, null, false), true);

            assertThat(result.active()).isFalse();
            assertThat(result.deactivatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Correo")
    class Email {

        @Test
        @DisplayName("se guarda en minusculas y sin espacios")
        void isNormalized() {
            saveReturnsArgument();

            PersonDTO result = service.create(newPerson("  Ana@UCOPET.com "));

            assertThat(result.email()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("uno vacio se guarda como null (y no se busca como duplicado)")
        void blankBecomesNull() {
            saveReturnsArgument();

            PersonDTO result = service.create(newPerson(""));

            assertThat(result.email()).isNull();
            verify(personRepository, never()).findByEmailIgnoreCase(any());
        }

        @Test
        @DisplayName("repetido con otras mayusculas sigue siendo un duplicado: 409")
        void duplicateIgnoringCase() {
            PersonDomain other = person(false, true, null);
            when(personRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(other));

            assertStatus(() -> service.create(newPerson("ANA@ucopet.com")), HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("el login no distingue mayusculas y avisa del inicio de sesion")
        void loginIgnoresCase() {
            PersonDomain ana = person(true, true, PASSWORD);
            when(personRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(ana));

            LoginResponseDTO response = service.login(new LoginRequestDTO("ANA@ucopet.com", PASSWORD));

            assertThat(response.roles()).containsExactly(Role.ADMIN);
            assertThat(response.token()).isNotBlank();
            verify(eventPublisher).publishEvent(any(LoginSuccededEvent.class));
        }
    }

    @Nested
    @DisplayName("Login y bloqueo")
    class Login {

        @Test
        @DisplayName("5 intentos fallidos bloquean la cuenta 15 minutos, aunque despues la clave sea buena")
        void fiveFailuresLockTheAccount() {
            PersonDomain ana = person(true, true, PASSWORD);
            when(personRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(ana));

            for (int i = 0; i < 5; i++) {
                assertStatus(() -> service.login(new LoginRequestDTO(EMAIL, "claveMala")),
                        HttpStatus.UNAUTHORIZED);
            }

            assertThat(ana.getFailedAttempts()).isEqualTo(5);
            assertThat(ana.getLockedUntil())
                    .isAfter(Instant.now().plus(14, ChronoUnit.MINUTES));
            assertStatus(() -> service.login(new LoginRequestDTO(EMAIL, PASSWORD)),
                    HttpStatus.LOCKED);
        }

        @Test
        @DisplayName("un correo que no existe tambien se bloquea a los 5 intentos")
        void unknownEmailIsLockedToo() {
            LoginRequestDTO request = new LoginRequestDTO("no.existe@ucopet.com", "loQueSea1");

            for (int i = 0; i < 5; i++) {
                assertStatus(() -> service.login(request), HttpStatus.UNAUTHORIZED);
            }

            assertStatus(() -> service.login(request), HttpStatus.LOCKED);
        }

        @Test
        @DisplayName("sin roles no recibe token, aunque la clave sea correcta")
        void noRolesNoToken() {
            PersonDomain ana = person(false, true, PASSWORD);
            when(personRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(ana));

            assertStatus(() -> service.login(new LoginRequestDTO(EMAIL, PASSWORD)),
                    HttpStatus.UNAUTHORIZED);
            verify(eventPublisher, never()).publishEvent(any(LoginSuccededEvent.class));
        }

        @Test
        @DisplayName("una cuenta dada de baja no entra, aunque la clave sea correcta")
        void inactiveAccountCannotLogIn() {
            PersonDomain ana = person(true, false, PASSWORD);
            when(personRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(ana));

            assertStatus(() -> service.login(new LoginRequestDTO(EMAIL, PASSWORD)),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("Cambiar la propia contrasena")
    class ChangeOwnPassword {

        @Test
        @DisplayName("exige la contrasena actual")
        void requiresCurrentPassword() {
            PersonDomain ana = person(true, true, PASSWORD);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));

            assertStatus(() -> service.changeOwnPassword(ana.getId(),
                            new ChangePasswordRequestDTO("claveMala", "claveNueva12", "claveNueva12")),
                    HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("con todo bien la cambia e invalida los tokens anteriores")
        void changesPasswordAndRevokesTokens() {
            PersonDomain ana = person(true, true, PASSWORD);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));

            service.changeOwnPassword(ana.getId(),
                    new ChangePasswordRequestDTO(PASSWORD, "claveNueva12", "claveNueva12"));

            assertThat(passwordEncoder.matches("claveNueva12", ana.getPasswordHash())).isTrue();
            assertThat(ana.getTokenVersion()).isEqualTo(1);
        }
    }

    // ---------------------------------------------------------------- helpers

    private PersonDomain person(boolean admin, boolean active, String rawPassword) {
        PersonDomain person = new PersonDomain();
        person.setId(UUID.randomUUID());
        person.setDocumentType(DocumentType.CC);
        person.setDocumentNumber("10203040");
        person.setFirstName("Ana");
        person.setLastName("Prueba");
        person.setEmail(EMAIL);
        person.setAdmin(admin);
        person.setActive(active);
        if (rawPassword != null) {
            person.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        return person;
    }

    private static PersonDTO dto(PersonDomain person, Boolean admin, Boolean active) {
        return new PersonDTO(person.getId(), person.getDocumentType(), person.getDocumentNumber(),
                person.getFirstName(), person.getLastName(), person.getEmail(),
                "Calle 1 # 2-3, Rionegro", "3000000000", admin, active, null);
    }

    private static PersonDTO newPerson(String email) {
        return new PersonDTO(null, DocumentType.CC, "10203040", "Ana", "Prueba", email,
                null, null, false, true, null);
    }

    private void saveReturnsArgument() {
        when(personRepository.saveAndFlush(any(PersonDomain.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static void assertStatus(ThrowingCallable call, HttpStatus expected) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }
}