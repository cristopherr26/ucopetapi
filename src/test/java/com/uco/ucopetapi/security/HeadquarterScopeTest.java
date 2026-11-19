package com.uco.ucopetapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;

@ExtendWith(MockitoExtension.class)
class HeadquarterScopeTest {

    private static final UUID PERSON = UUID.randomUUID();
    private static final UUID OWN_HEADQUARTER = UUID.randomUUID();
    private static final UUID OTHER_HEADQUARTER = UUID.randomUUID();

    @Mock
    private IDoctorRepository doctorRepository;

    private HeadquarterScope scope;

    @BeforeEach
    void setUp() {
        scope = new HeadquarterScope(doctorRepository);
    }

    @AfterEach
    void clearSession() {
        SecurityContextHolder.clearContext();
    }

    private static void signIn(final Role... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority(role.authority()))
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(PERSON.toString(), null, authorities));
    }

    private void worksAt(final UUID headquarterId) {
        when(doctorRepository.findByIdPerson(PERSON))
                .thenReturn(List.of(new DoctorDomain(UUID.randomUUID(), PERSON, "MV-1", headquarterId)));
    }

    @Test
    @DisplayName("El administrador lee cualquier sede sin consultar médicos")
    void adminReadsAnyHeadquarter() {
        signIn(Role.ADMIN);

        assertThatCode(() -> scope.requireReadable(OTHER_HEADQUARTER)).doesNotThrowAnyException();
        verifyNoInteractions(doctorRepository);
    }

    @Test
    @DisplayName("El médico lee su propia sede")
    void doctorReadsOwnHeadquarter() {
        signIn(Role.DOCTOR);
        worksAt(OWN_HEADQUARTER);

        assertThatCode(() -> scope.requireReadable(OWN_HEADQUARTER)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("El médico recibe 403 al pedir otra sede")
    void doctorCannotReadOtherHeadquarter() {
        signIn(Role.DOCTOR);
        worksAt(OWN_HEADQUARTER);

        assertThatThrownBy(() -> scope.requireReadable(OTHER_HEADQUARTER))
                .isInstanceOfSatisfying(HeadquarterForbiddenException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(exception.getReason()).isEqualTo(HeadquarterScope.OTHER_HEADQUARTER);
                });
    }

    @Test
    @DisplayName("Una sesión sin rol de médico ni de administrador no lee ninguna sede")
    void personWithoutRoleCannotRead() {
        signIn();
        when(doctorRepository.findByIdPerson(PERSON)).thenReturn(List.of());

        assertThatThrownBy(() -> scope.requireReadable(OWN_HEADQUARTER))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("Sin sesión no decide nada: eso lo resuelve SecurityConfig")
    void withoutSessionLeavesItToSecurityConfig() {
        assertThatCode(() -> scope.requireReadable(OTHER_HEADQUARTER)).doesNotThrowAnyException();
        verifyNoInteractions(doctorRepository);
    }
}
