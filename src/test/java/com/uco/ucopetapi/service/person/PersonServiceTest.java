package com.uco.ucopetapi.service.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.mockito.ArgumentCaptor;
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
import com.uco.ucopetapi.dto.person.PersonMessageDTO;
import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.event.LoginSuccededEvent;
import com.uco.ucopetapi.messaging.person.PersonCreationSenderBroker;
import com.uco.ucopetapi.messaging.person.PersonDeactivationSenderBroker;
import com.uco.ucopetapi.messaging.person.PersonUpdateSenderBroker;
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

    @Mock
    private PersonCreationSenderBroker creationSender;

    @Mock
    private PersonUpdateSenderBroker updateSender;

    @Mock
    private PersonDeactivationSenderBroker deactivationSender;

    // BCrypt real pero con fuerza 4 (el minimo) para que los tests sean rapidos.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private PersonService service;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService("clave-de-pruebas-de-al-menos-32-caracteres", 60);
        service = new PersonService(personRepository, passwordEncoder, jwtService,
                new UnknownEmailAttempts(), doctorRepository, eventPublisher,
                creationSender, updateSender, deactivationSender);
    }

    @Nested
    @DisplayName("Editar una persona (publica en la cola, no guarda)")
    class Update {

        @Test
        @DisplayName("si el JSON no trae 'active', viaja en null y la respuesta la muestra activa")
        void missingActiveTravelsAsNull() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));

            PersonDTO result = service.update(ana.getId(), dto(ana, null, null), true);

            assertThat(sentUpdate().active()).isNull();
            assertThat(result.active()).isTrue();
            verify(personRepository, never()).save(any(PersonDomain.class));
        }

        @Test
        @DisplayName("quien no es ADMIN manda admin y active en null: no se asciende ni se da de baja")
        void nonAdminSendsNullFlags() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));

            PersonDTO result = service.update(ana.getId(), dto(ana, true, false), false);

            PersonMessageDTO message = sentUpdate();
            assertThat(message.admin()).isNull();
            assertThat(message.active()).isNull();
            assertThat(result.admin()).isFalse();
            assertThat(result.active()).isTrue();
        }

        @Test
        @DisplayName("el ADMIN si puede mandar el cambio de rol")
        void adminCanGrantAdminRole() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));

            PersonDTO result = service.update(ana.getId(), dto(ana, true, true), true);

            assertThat(sentUpdate().admin()).isTrue();
            assertThat(result.admin()).isTrue();
        }

        @Test
        @DisplayName("quitarle el rol al unico ADMIN activo da 409 y no se publica nada")
        void cannotRevokeLastAdmin() {
            PersonDomain admin = person(true, true, null);
            when(personRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(personRepository.countByAdminTrueAndActiveTrue()).thenReturn(1L);

            assertStatus(() -> service.update(admin.getId(), dto(admin, false, true), true),
                    HttpStatus.CONFLICT);
            verify(updateSender, never()).execute(any(PersonMessageDTO.class), anyString());
        }

        @Test
        @DisplayName("una persona que no existe da 404 y no se publica nada")
        void unknownPersonIs404() {
            UUID id = UUID.randomUUID();
            when(personRepository.findById(id)).thenReturn(Optional.empty());

            assertStatus(() -> service.update(id, dto(person(false, true, null), null, null), true),
                    HttpStatus.NOT_FOUND);
            verify(updateSender, never()).execute(any(PersonMessageDTO.class), anyString());
        }

        @Test
        @DisplayName("dar de baja publica un mensaje solo con el id")
        void deactivate() {
            PersonDomain ana = person(false, true, null);
            when(personRepository.findById(ana.getId())).thenReturn(Optional.of(ana));

            service.delete(ana.getId());

            ArgumentCaptor<PersonMessageDTO> captor = ArgumentCaptor.forClass(PersonMessageDTO.class);
            verify(deactivationSender).execute(captor.capture(), anyString());
            assertThat(captor.getValue().id()).isEqualTo(ana.getId());
            assertThat(captor.getValue().documentNumber()).isNull();
        }
    }

    @Nested
    @DisplayName("Correo")
    class Email {

        @Test
        @DisplayName("se publica en minusculas y sin espacios, con un id nuevo y activa")
        void isNormalized() {
            PersonDTO result = service.create(newPerson("  Ana@UCOPET.com "));

            ArgumentCaptor<PersonMessageDTO> captor = ArgumentCaptor.forClass(PersonMessageDTO.class);
            verify(creationSender).execute(captor.capture(), anyString());
            PersonMessageDTO message = captor.getValue();
            assertThat(message.email()).isEqualTo(EMAIL);
            assertThat(message.id()).isNotNull();
            assertThat(message.active()).isTrue();
            assertThat(result.id()).isEqualTo(message.id());
            verify(personRepository, never()).save(any(PersonDomain.class));
        }

        @Test
        @DisplayName("uno vacio viaja como null (y no se busca como duplicado)")
        void blankBecomesNull() {
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
            verify(creationSender, never()).execute(any(PersonMessageDTO.class), anyString());
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

    private PersonMessageDTO sentUpdate() {
        ArgumentCaptor<PersonMessageDTO> captor = ArgumentCaptor.forClass(PersonMessageDTO.class);
        verify(updateSender).execute(captor.capture(), anyString());
        return captor.getValue();
    }

    private static void assertStatus(ThrowingCallable call, HttpStatus expected) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }
}