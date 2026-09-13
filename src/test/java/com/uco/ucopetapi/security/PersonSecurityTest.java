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
import com.uco.ucopetapi.repository.person.PersonRepository;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Quien puede hacer que")
class PersonSecurityTest {

    static final String ADMIN = "admin@ucopet.com";
    static final String CLAVE_ADMIN = "claveDeArranquePrueba";
    private static final String CLAVE_SIN_ROLES = "claveSinRolPrueba";

    private String sinRoles;

    @LocalServerPort
    private int puerto;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HttpClient http = HttpClient.newHttpClient();

    private String claveAdminOriginal;
    private String tokenAdmin;
    private String idAdmin;
    private String idSinRoles;

    @BeforeAll
    void prepararDatos() throws Exception {
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        claveAdminOriginal = admin.getPasswordHash();
        admin.setPasswordHash(passwordEncoder.encode(CLAVE_ADMIN));
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        personRepository.save(admin);

        tokenAdmin = tokenDe(ADMIN, CLAVE_ADMIN);
        idAdmin = campo(pedir("GET", "/persons?email=" + ADMIN, null, tokenAdmin).body(), "id");

        String doc = "9100" + System.currentTimeMillis() % 10000;
        sinRoles = "sin.rol." + doc + "@ucopet.com";
        idSinRoles = campo(pedir("POST", "/persons", personaJson(doc, "sin.rol." + doc),
                tokenAdmin).body(), "id");
        pedir("PUT", "/persons/" + idSinRoles + "/password",
                "{\"password\":\"" + CLAVE_SIN_ROLES + "\"}", tokenAdmin);
    }

    @AfterAll
    void limpiar() throws Exception {
        pedir("DELETE", "/persons/" + idSinRoles, null, tokenAdmin);
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        admin.setPasswordHash(claveAdminOriginal);
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        personRepository.save(admin);
    }

    @Nested
    @DisplayName("Sin token")
    class SinToken {

        @Test
        @DisplayName("no puede ver las personas")
        void noVe() throws Exception {
            assertThat(pedir("GET", "/persons", null, null).statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("no puede crear una persona")
        void noCrea() throws Exception {
            assertThat(pedir("POST", "/persons", personaJson("90010001", "sin.token"), null)
                    .statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("NO puede ver los tipos de documento")
        void noVeLosTiposDeDocumento() throws Exception {
            assertThat(pedir("GET", "/persons/document-types", null, null).statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("si puede pedir un token")
        void puedeLoguearse() throws Exception {
            assertThat(pedir("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"" + CLAVE_ADMIN + "\"}", null)
                    .statusCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Una persona sin roles")
    class SinRoles {

        @Test
        @DisplayName("NO recibe token, aunque la contrasena sea correcta")
        void noRecibeToken() throws Exception {
            HttpResponse<String> r = pedir("POST", "/persons/login",
                    "{\"email\":\"" + sinRoles + "\",\"password\":\"" + CLAVE_SIN_ROLES + "\"}", null);
            assertThat(r.statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("y el error es el MISMO que con la contrasena mala: no delata la cuenta")
        void noDelataQueLaCuentaExiste() throws Exception {
            HttpResponse<String> sinRol = pedir("POST", "/persons/login",
                    "{\"email\":\"" + sinRoles + "\",\"password\":\"" + CLAVE_SIN_ROLES + "\"}", null);
            HttpResponse<String> claveMala = pedir("POST", "/persons/login",
                    "{\"email\":\"" + sinRoles + "\",\"password\":\"estaEstaMal\"}", null);

            assertThat(sinRol.statusCode()).isEqualTo(claveMala.statusCode());
        }
    }

    @Nested
    @DisplayName("El administrador")
    class Admin {

        @Test
        @DisplayName("puede ver las personas")
        void ve() throws Exception {
            assertThat(pedir("GET", "/persons", null, tokenAdmin).statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("y los tipos de documento")
        void veLosTiposDeDocumento() throws Exception {
            assertThat(pedir("GET", "/persons/document-types", null, tokenAdmin).statusCode())
                    .isEqualTo(200);
        }

        @Test
        @DisplayName("crea y da de baja")
        void creaYBorra() throws Exception {
            String doc = "9002" + System.currentTimeMillis() % 10000;
            HttpResponse<String> alta =
                    pedir("POST", "/persons", personaJson(doc, "admin.crea." + doc), tokenAdmin);
            assertThat(alta.statusCode()).isEqualTo(201);

            String id = campo(alta.body(), "id");
            assertThat(pedir("DELETE", "/persons/" + id, null, tokenAdmin).statusCode())
                    .isEqualTo(204);
        }

        @Test
        @DisplayName("puede resetear la contrasena de otro sin conocerla")
        void reseteaLaDeOtro() throws Exception {
            assertThat(pedir("PUT", "/persons/" + idSinRoles + "/password",
                    "{\"password\":\"" + CLAVE_SIN_ROLES + "\"}", tokenAdmin).statusCode())
                    .isEqualTo(204);
        }

        @Test
        @DisplayName("NO puede darse de baja si es el unico admin")
        void noSeBorraSiEsElUnico() throws Exception {
            assertThat(pedir("DELETE", "/persons/" + idAdmin, null, tokenAdmin).statusCode())
                    .isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("El token")
    class Token {

        @Test
        @DisplayName("lleva el personId en sub y los roles")
        void lleva() throws Exception {
            String cuerpo = pedir("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"" + CLAVE_ADMIN + "\"}", null).body();
            assertThat(cuerpo).contains("\"roles\":[\"ADMIN\"]");
            assertThat(campo(cuerpo, "token").split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("con la firma adulterada no autentica")
        void firmaAdulterada() throws Exception {
            assertThat(pedir("GET", "/persons", null, tokenAdmin + "xx").statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("una contrasena mala y un correo inexistente dan el MISMO error")
        void mismoError() throws Exception {
            HttpResponse<String> malaClave = pedir("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"estaEstaMal\"}", null);
            HttpResponse<String> noExiste = pedir("POST", "/persons/login",
                    "{\"email\":\"nadie" + UUID.randomUUID() + "@ucopet.com\",\"password\":\"estaEstaMal\"}", null);

            assertThat(malaClave.statusCode()).isEqualTo(401);
            assertThat(noExiste.statusCode()).isEqualTo(malaClave.statusCode());
        }
    }

    @Nested
    @DisplayName("Cambiar la propia contrasena")
    class CambioPropio {

        @Test
        @DisplayName("exige la contrasena ACTUAL: un token robado no alcanza")
        void exigeLaActual() throws Exception {
            assertThat(pedir("PUT", "/persons/me/password",
                    cambio("noEsLaMia", "claveNueva123", "claveNueva123"), tokenAdmin).statusCode())
                    .isEqualTo(401);
        }

        @Test
        @DisplayName("la confirmacion tiene que coincidir")
        void confirmacionDistinta() throws Exception {
            assertThat(pedir("PUT", "/persons/me/password",
                    cambio(CLAVE_ADMIN, "claveNueva123", "otraCosa999"), tokenAdmin).statusCode())
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("la nueva no puede ser la misma de antes")
        void mismaDeAntes() throws Exception {
            assertThat(pedir("PUT", "/persons/me/password",
                    cambio(CLAVE_ADMIN, CLAVE_ADMIN, CLAVE_ADMIN), tokenAdmin).statusCode())
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("con todo bien la cambia, y despues la deja como estaba")
        void cambiaYRestaura() throws Exception {
            assertThat(pedir("PUT", "/persons/me/password",
                    cambio(CLAVE_ADMIN, "claveNueva123", "claveNueva123"), tokenAdmin).statusCode())
                    .isEqualTo(204);

            assertThat(pedir("POST", "/persons/login",
                    "{\"email\":\"" + ADMIN + "\",\"password\":\"" + CLAVE_ADMIN + "\"}", null)
                    .statusCode()).isEqualTo(401);

            String nuevo = tokenDe(ADMIN, "claveNueva123");
            assertThat(pedir("PUT", "/persons/me/password",
                    cambio("claveNueva123", CLAVE_ADMIN, CLAVE_ADMIN), nuevo).statusCode())
                    .isEqualTo(204);

            tokenAdmin = tokenDe(ADMIN, CLAVE_ADMIN);
        }

        @Test
        @DisplayName("cambiarla mata los tokens que ya estaban emitidos")
        void cambiarlaCierraLasSesiones() throws Exception {
            String viejo = tokenDe(ADMIN, CLAVE_ADMIN);
            assertThat(pedir("GET", "/persons/me", null, viejo).statusCode()).isEqualTo(200);

            assertThat(pedir("PUT", "/persons/me/password",
                    cambio(CLAVE_ADMIN, "otraClave456", "otraClave456"), viejo).statusCode())
                    .isEqualTo(204);

            assertThat(pedir("GET", "/persons/me", null, viejo).statusCode()).isEqualTo(401);

            String nuevo = tokenDe(ADMIN, "otraClave456");
            assertThat(pedir("PUT", "/persons/me/password",
                    cambio("otraClave456", CLAVE_ADMIN, CLAVE_ADMIN), nuevo).statusCode())
                    .isEqualTo(204);

            tokenAdmin = tokenDe(ADMIN, CLAVE_ADMIN);
        }

        @Test
        @DisplayName("sin token no se puede")
        void sinToken() throws Exception {
            assertThat(pedir("PUT", "/persons/me/password",
                    cambio(CLAVE_ADMIN, "claveNueva123", "claveNueva123"), null).statusCode())
                    .isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("Enumeracion de usuarios")
    class Enumeracion {

        @Test
        @DisplayName("un correo que NO existe se bloquea igual que uno real")
        void elBloqueoNoDelataQuienExiste() throws Exception {
            String inventado = "no.existe." + System.currentTimeMillis() + "@ucopet.com";

            for (int i = 0; i < 5; i++) {
                assertThat(pedir("POST", "/persons/login",
                        "{\"email\":\"" + inventado + "\",\"password\":\"loQueSea\"}", null)
                        .statusCode()).isEqualTo(401);
            }

            assertThat(pedir("POST", "/persons/login",
                    "{\"email\":\"" + inventado + "\",\"password\":\"loQueSea\"}", null)
                    .statusCode()).isEqualTo(423);
        }

        @Test
        @DisplayName("cada correo cuenta sus propios intentos")
        void losIntentosNoSeMezclan() throws Exception {
            String uno = "uno." + System.currentTimeMillis() + "@ucopet.com";
            String otro = "otro." + System.currentTimeMillis() + "@ucopet.com";

            for (int i = 0; i < 6; i++) {
                pedir("POST", "/persons/login",
                        "{\"email\":\"" + uno + "\",\"password\":\"loQueSea\"}", null);
            }

            assertThat(pedir("POST", "/persons/login",
                    "{\"email\":\"" + uno + "\",\"password\":\"loQueSea\"}", null)
                    .statusCode()).isEqualTo(423);
            assertThat(pedir("POST", "/persons/login",
                    "{\"email\":\"" + otro + "\",\"password\":\"loQueSea\"}", null)
                    .statusCode()).isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("Cerrar sesion")
    class CerrarSesion {

        @Test
        @DisplayName("el token deja de servir en toda la API, no solo en person")
        void revocaDeVerdad() throws Exception {
            String suyo = tokenDe(ADMIN, CLAVE_ADMIN);
            assertThat(pedir("GET", "/persons/me", null, suyo).statusCode()).isEqualTo(200);

            assertThat(pedir("POST", "/persons/logout", null, suyo).statusCode()).isEqualTo(204);

            assertThat(pedir("GET", "/persons/me", null, suyo).statusCode()).isEqualTo(401);
            assertThat(pedir("GET", "/pets", null, suyo).statusCode()).isEqualTo(401);

            tokenAdmin = tokenDe(ADMIN, CLAVE_ADMIN);
        }

        @Test
        @DisplayName("no bloquea la cuenta: se puede volver a entrar")
        void sePuedeVolverAEntrar() throws Exception {
            String suyo = tokenDe(ADMIN, CLAVE_ADMIN);
            assertThat(pedir("POST", "/persons/logout", null, suyo).statusCode()).isEqualTo(204);

            tokenAdmin = tokenDe(ADMIN, CLAVE_ADMIN);
            assertThat(pedir("GET", "/persons/me", null, tokenAdmin).statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("sin token no se puede cerrar sesion")
        void sinToken() throws Exception {
            assertThat(pedir("POST", "/persons/logout", null, null).statusCode()).isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("Validacion de entrada")
    class Validacion {

        @Test
        @DisplayName("tipo de documento inventado")
        void tipoInventado() throws Exception {
            assertThat(pedir("POST", "/persons",
                    personaJson("90010003", "tipo.malo").replace("\"CC\"", "\"INVENTADO\""), tokenAdmin)
                    .statusCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("nombre vacio")
        void nombreVacio() throws Exception {
            assertThat(pedir("POST", "/persons",
                    personaJson("90010004", "sin.nombre").replace("\"Prueba\"", "\"\""), tokenAdmin)
                    .statusCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("contrasena de mas de 72: la limita BCrypt")
        void contrasenaLarguisima() throws Exception {
            assertThat(pedir("PUT", "/persons/" + idSinRoles + "/password",
                    "{\"password\":\"" + "a".repeat(200) + "\"}", tokenAdmin).statusCode())
                    .isEqualTo(400);
        }
    }

    private String tokenDe(String correo, String clave) throws Exception {
        return campo(pedir("POST", "/persons/login",
                "{\"email\":\"" + correo + "\",\"password\":\"" + clave + "\"}", null).body(), "token");
    }

    private HttpResponse<String> pedir(String metodo, String ruta, String cuerpo, String token)
            throws Exception {
        HttpRequest.BodyPublisher publicador = cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo);

        HttpRequest.Builder peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + "/api/v1" + ruta))
                .method(metodo, publicador)
                .header("Content-Type", "application/json");

        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String cambio(String actual, String nueva, String confirmacion) {
        return """
                {"currentPassword":"%s","newPassword":"%s","confirmPassword":"%s"}
                """.formatted(actual, nueva, confirmacion);
    }

    private static String personaJson(String documento, String correo) {
        return """
                {"documentType":"CC","documentNumber":"%s","firstName":"Prueba",
                 "lastName":"Seguridad","email":"%s@ucopet.com","admin":false,"active":true}
                """.formatted(documento, correo);
    }

    private static String campo(String json, String nombre) {
        int desde = json.indexOf("\"" + nombre + "\":\"") + nombre.length() + 4;
        return json.substring(desde, json.indexOf('"', desde));
    }
}
