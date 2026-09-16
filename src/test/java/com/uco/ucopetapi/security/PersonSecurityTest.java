package com.uco.ucopetapi.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Quien puede hacer que")
class PersonSecurityTest {

    static final String ADMIN = "admin@ucopet.com";
    static final String ADMIN_PASSWORD = "claveDeArranquePrueba";
    private static final String NO_ROLE_PASSWORD = "claveSinRolPrueba";

    private String noRoleEmail;

    @LocalServerPort
    private int port;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IDoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HttpClient http = HttpClient.newHttpClient();

    private String originalAdminHash;
    private String adminToken;
    private String adminId;
    private String noRoleId;

    @BeforeAll
    void prepareData() throws Exception {
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        originalAdminHash = admin.getPasswordHash();
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        personRepository.save(admin);

        adminToken = tokenFor(ADMIN, ADMIN_PASSWORD);
        adminId = field(call("GET", "/persons?email=" + ADMIN, null, adminToken).body(), "id");

        String doc = "9100" + System.currentTimeMillis() % 10000;
        noRoleEmail = "sin.rol." + doc + "@ucopet.com";
        noRoleId = field(call("POST", "/persons", personJson(doc, "sin.rol." + doc),
                adminToken).body(), "id");
        call("PUT", "/persons/" + noRoleId + "/password",
                "{\"password\":\"" + NO_ROLE_PASSWORD + "\"}", adminToken);
    }

    @AfterAll
    void cleanUp() throws Exception {
        call("DELETE", "/persons/" + noRoleId, null, adminToken);
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        admin.setPasswordHash(originalAdminHash);
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        personRepository.save(admin);
    }

    @Nested
    @DisplayName("Sin token")
    class WithoutToken {

        @Test
        @DisplayName("no puede ver las personas")
        void noVe() throws Exception {
            assertThat(call("GET", "/persons", null, null).statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("no puede crear una persona")
        void noCrea() throws Exception {
            assertThat(call("POST", "/persons", personJson("90010001", "sin.token"), null)
                    .statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("NO puede ver los tipos de documento")
        void noVeLosTiposDeDocumento() throws Exception {
            assertThat(call("GET", "/persons/document-types", null, null).statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("si puede pedir un token")
        void puedeLoguearse() throws Exception {
            assertThat(call("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}", null)
                    .statusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Una persona sin roles")
    class SinRoles {

        @Test
        @DisplayName("NO recibe token, aunque la contrasena sea correcta")
        void noRecibeToken() throws Exception {
            HttpResponse<String> r = call("POST", "/persons/login",
                    "{\"email\":\"" + noRoleEmail + "\",\"password\":\"" + NO_ROLE_PASSWORD + "\"}", null);
            assertThat(r.statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("y el error es el MISMO que con la contrasena mala: no delata la cuenta")
        void noDelataQueLaCuentaExiste() throws Exception {
            HttpResponse<String> sinRol = call("POST", "/persons/login",
                    "{\"email\":\"" + noRoleEmail + "\",\"password\":\"" + NO_ROLE_PASSWORD + "\"}", null);
            HttpResponse<String> claveMala = call("POST", "/persons/login",
                    "{\"email\":\"" + noRoleEmail + "\",\"password\":\"estaEstaMal\"}", null);

            assertThat(sinRol.statusCode()).isEqualTo(claveMala.statusCode());
        }
    }

    @Nested
    @DisplayName("El administrador")
    class Admin {

        @Test
        @DisplayName("puede ver las personas")
        void ve() throws Exception {
            assertThat(call("GET", "/persons", null, adminToken).statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("y los tipos de documento")
        void veLosTiposDeDocumento() throws Exception {
            assertThat(call("GET", "/persons/document-types", null, adminToken).statusCode())
                    .isEqualTo(200);
        }

        @Test
        @DisplayName("crea y da de baja")
        void creaYBorra() throws Exception {
            String doc = "9002" + System.currentTimeMillis() % 10000;
            HttpResponse<String> alta =
                    call("POST", "/persons", personJson(doc, "admin.crea." + doc), adminToken);
            assertThat(alta.statusCode()).isEqualTo(201);

            String id = field(alta.body(), "id");
            assertThat(call("DELETE", "/persons/" + id, null, adminToken).statusCode())
                    .isEqualTo(204);
        }

        @Test
        @DisplayName("puede resetear la contrasena de otro sin conocerla")
        void reseteaLaDeOtro() throws Exception {
            assertThat(call("PUT", "/persons/" + noRoleId + "/password",
                    "{\"password\":\"" + NO_ROLE_PASSWORD + "\"}", adminToken).statusCode())
                    .isEqualTo(204);
        }

        @Test
        @DisplayName("NO puede darse de baja si es el unico admin")
        void noSeBorraSiEsElUnico() throws Exception {
            assertThat(call("DELETE", "/persons/" + adminId, null, adminToken).statusCode())
                    .isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("El token")
    class Token {

        @Test
        @DisplayName("lleva el personId en sub y los roles")
        void lleva() throws Exception {
            String body = call("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}", null).body();
            assertThat(body).contains("\"roles\":[\"ADMIN\"]");
            assertThat(field(body, "token").split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("con la firma adulterada no autentica")
        void firmaAdulterada() throws Exception {
            assertThat(call("GET", "/persons", null, adminToken + "xx").statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("una contrasena mala y un correo inexistente dan el MISMO error")
        void mismoError() throws Exception {
            HttpResponse<String> malaClave = call("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"estaEstaMal\"}", null);
            HttpResponse<String> noExiste = call("POST", "/persons/login",
                    "{\"email\":\"nadie" + UUID.randomUUID() + "@ucopet.com\",\"password\":\"estaEstaMal\"}", null);

            assertThat(malaClave.statusCode()).isEqualTo(401);
            assertThat(noExiste.statusCode()).isEqualTo(malaClave.statusCode());
        }
    }

    @Nested
    @DisplayName("Cambiar la propia contrasena")
    class OwnPasswordChange {

        @Test
        @DisplayName("exige la contrasena ACTUAL: un token robado no alcanza")
        void exigeLaActual() throws Exception {
            assertThat(call("PUT", "/persons/me/password",
                    passwordChange("noEsLaMia", "claveNueva123", "claveNueva123"), adminToken).statusCode())
                    .isEqualTo(401);
        }

        @Test
        @DisplayName("la confirmacion tiene que coincidir")
        void confirmacionDistinta() throws Exception {
            assertThat(call("PUT", "/persons/me/password",
                    passwordChange(ADMIN_PASSWORD, "claveNueva123", "otraCosa999"), adminToken).statusCode())
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("la nueva no puede ser la misma de antes")
        void mismaDeAntes() throws Exception {
            assertThat(call("PUT", "/persons/me/password",
                    passwordChange(ADMIN_PASSWORD, ADMIN_PASSWORD, ADMIN_PASSWORD), adminToken).statusCode())
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("con todo bien la cambia, y despues la deja como estaba")
        void cambiaYRestaura() throws Exception {
            assertThat(call("PUT", "/persons/me/password",
                    passwordChange(ADMIN_PASSWORD, "claveNueva123", "claveNueva123"), adminToken).statusCode())
                    .isEqualTo(204);

            assertThat(call("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}", null)
                    .statusCode()).isEqualTo(401);

            String newToken = tokenFor(ADMIN, "claveNueva123");
            assertThat(call("PUT", "/persons/me/password",
                    passwordChange("claveNueva123", ADMIN_PASSWORD, ADMIN_PASSWORD), newToken).statusCode())
                    .isEqualTo(204);

            adminToken = tokenFor(ADMIN, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("cambiarla mata los tokens que ya estaban emitidos")
        void cambiarlaCierraLasSesiones() throws Exception {
            String oldToken = tokenFor(ADMIN, ADMIN_PASSWORD);
            assertThat(call("GET", "/persons/me", null, oldToken).statusCode()).isEqualTo(200);

            assertThat(call("PUT", "/persons/me/password",
                    passwordChange(ADMIN_PASSWORD, "otraClave456", "otraClave456"), oldToken).statusCode())
                    .isEqualTo(204);

            assertThat(call("GET", "/persons/me", null, oldToken).statusCode()).isEqualTo(401);

            String newToken = tokenFor(ADMIN, "otraClave456");
            assertThat(call("PUT", "/persons/me/password",
                    passwordChange("otraClave456", ADMIN_PASSWORD, ADMIN_PASSWORD), newToken).statusCode())
                    .isEqualTo(204);

            adminToken = tokenFor(ADMIN, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("sin token no se puede")
        void sinToken() throws Exception {
            assertThat(call("PUT", "/persons/me/password",
                    passwordChange(ADMIN_PASSWORD, "claveNueva123", "claveNueva123"), null).statusCode())
                    .isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("Enumeracion de usuarios")
    class UserEnumeration {

        @Test
        @DisplayName("un correo que NO existe se bloquea igual que uno real")
        void elBloqueoNoDelataQuienExiste() throws Exception {
            String madeUp = "no.existe." + System.currentTimeMillis() + "@ucopet.com";

            for (int i = 0; i < 5; i++) {
                assertThat(call("POST", "/persons/login",
                        "{\"email\":\"" + madeUp + "\",\"password\":\"loQueSea\"}", null)
                        .statusCode()).isEqualTo(401);
            }

            assertThat(call("POST", "/persons/login",
                    "{\"email\":\"" + madeUp + "\",\"password\":\"loQueSea\"}", null)
                    .statusCode()).isEqualTo(423);
        }

        @Test
        @DisplayName("cada correo cuenta sus propios intentos")
        void losIntentosNoSeMezclan() throws Exception {
            String first = "first." + System.currentTimeMillis() + "@ucopet.com";
            String second = "second." + System.currentTimeMillis() + "@ucopet.com";

            for (int i = 0; i < 6; i++) {
                call("POST", "/persons/login",
                        "{\"email\":\"" + first + "\",\"password\":\"loQueSea\"}", null);
            }

            assertThat(call("POST", "/persons/login",
                    "{\"email\":\"" + first + "\",\"password\":\"loQueSea\"}", null)
                    .statusCode()).isEqualTo(423);
            assertThat(call("POST", "/persons/login",
                    "{\"email\":\"" + second + "\",\"password\":\"loQueSea\"}", null)
                    .statusCode()).isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("El rol DOCTOR sale de la tabla doctors")
    class DoctorRole {

        @Test
        @DisplayName("sin fila en doctors no entra; con fila entra como DOCTOR")
        void elRolApareceConLaFila() throws Exception {
            String doc = "9300" + System.currentTimeMillis() % 10000;
            String email = "medica." + doc + "@ucopet.com";
            String id = field(call("POST", "/persons", personJson(doc, "medica." + doc),
                    adminToken).body(), "id");
            call("PUT", "/persons/" + id + "/password",
                    "{\"password\":\"" + NO_ROLE_PASSWORD + "\"}", adminToken);

            String credentials = "{\"email\":\"" + email + "\",\"password\":\""
                    + NO_ROLE_PASSWORD + "\"}";
            assertThat(call("POST", "/persons/login", credentials, null).statusCode())
                    .isEqualTo(401);

            DoctorDomain row = new DoctorDomain(null, UUID.fromString(id), "MED-" + doc);
            doctorRepository.save(row);
            try {
                var response = call("POST", "/persons/login", credentials, null);
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.body()).contains("\"DOCTOR\"");

                String herToken = field(response.body(), "token");
                assertThat(call("GET", "/persons", null, herToken).statusCode()).isEqualTo(200);
                assertThat(call("GET", "/episodes", null, herToken).statusCode()).isEqualTo(200);
                assertThat(call("POST", "/persons", personJson("93999999", "otra"), herToken)
                        .statusCode()).isEqualTo(403);
                assertThat(call("GET", "/purchases", null, herToken).statusCode()).isEqualTo(403);
            } finally {
                doctorRepository.delete(row);
                call("DELETE", "/persons/" + id, null, adminToken);
            }
        }
    }

    @Nested
    @DisplayName("Cerrar sesion")
    class Logout {

        @Test
        @DisplayName("el token deja de servir en toda la API, no solo en person")
        void revocaDeVerdad() throws Exception {
            String ownToken = tokenFor(ADMIN, ADMIN_PASSWORD);
            assertThat(call("GET", "/persons/me", null, ownToken).statusCode()).isEqualTo(200);

            assertThat(call("POST", "/persons/logout", null, ownToken).statusCode()).isEqualTo(204);

            assertThat(call("GET", "/persons/me", null, ownToken).statusCode()).isEqualTo(401);
            assertThat(call("GET", "/pets", null, ownToken).statusCode()).isEqualTo(401);

            adminToken = tokenFor(ADMIN, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("no bloquea la cuenta: se puede volver a entrar")
        void sePuedeVolverAEntrar() throws Exception {
            String ownToken = tokenFor(ADMIN, ADMIN_PASSWORD);
            assertThat(call("POST", "/persons/logout", null, ownToken).statusCode()).isEqualTo(204);

            adminToken = tokenFor(ADMIN, ADMIN_PASSWORD);
            assertThat(call("GET", "/persons/me", null, adminToken).statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("sin token no se puede cerrar sesion")
        void sinToken() throws Exception {
            assertThat(call("POST", "/persons/logout", null, null).statusCode()).isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("Validacion de entrada")
    class InputValidation {

        @Test
        @DisplayName("tipo de documento inventado")
        void tipoInventado() throws Exception {
            assertThat(call("POST", "/persons",
                    personJson("90010003", "tipo.malo").replace("\"CC\"", "\"INVENTADO\""), adminToken)
                    .statusCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("nombre vacio")
        void nombreVacio() throws Exception {
            assertThat(call("POST", "/persons",
                    personJson("90010004", "sin.nombre").replace("\"Prueba\"", "\"\""), adminToken)
                    .statusCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("contrasena de mas de 72: la limita BCrypt")
        void contrasenaLarguisima() throws Exception {
            assertThat(call("PUT", "/persons/" + noRoleId + "/password",
                    "{\"password\":\"" + "a".repeat(200) + "\"}", adminToken).statusCode())
                    .isEqualTo(400);
        }
    }

    private String tokenFor(String email, String password) throws Exception {
        return field(call("POST", "/persons/login",
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}", null).body(), "token");
    }

    private HttpResponse<String> call(String method, String path, String body, String token)
            throws Exception {
        HttpRequest.BodyPublisher bodyPublisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1" + path))
                .method(method, bodyPublisher)
                .header("Content-Type", "application/json");

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String passwordChange(String current, String updated, String confirmation) {
        return """
                {"currentPassword":"%s","newPassword":"%s","confirmPassword":"%s"}
                """.formatted(current, updated, confirmation);
    }

    private static String personJson(String documentNumber, String email) {
        return """
                {"documentType":"CC","documentNumber":"%s","firstName":"Prueba",
                 "lastName":"Seguridad","email":"%s@ucopet.com","admin":false,"active":true}
                """.formatted(documentNumber, email);
    }

    private static String field(String json, String name) {
        int from = json.indexOf("\"" + name + "\":\"") + name.length() + 4;
        return json.substring(from, json.indexOf('"', from));
    }
}
