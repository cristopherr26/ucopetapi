package com.uco.ucopetapi.controllers.transfer;

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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.product.StockService;

/**
 * Pruebas de integracion end-to-end del modulo Transfers: levantan la aplicacion
 * completa (Tomcat real, filtro JWT real, Postgres real via docker-compose) y
 * golpean los endpoints por HTTP, igual que hace PersonSecurityTest para Person.
 *
 * Requiere que el contenedor de Postgres (docker compose up -d) este corriendo,
 * y que exista el usuario admin sembrado por PersonSeeder (admin@ucopet.com).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Transfers - pruebas de integracion")
class TransferIntegrationTest {

    private static final String ADMIN = "admin@ucopet.com";
    private static final String CLAVE_ADMIN = "claveDeArranquePrueba";

    @LocalServerPort
    private int puerto;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    private final HttpClient http = HttpClient.newHttpClient();

    private String claveAdminOriginal;
    private String tokenAdmin;

    private UUID sedeOrigenId;
    private UUID sedeDestinoId;
    private UUID sedeInactivaId;
    private UUID productoId;

    @BeforeAll
    void prepararDatos() throws Exception {
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        claveAdminOriginal = admin.getPasswordHash();
        admin.setPasswordHash(passwordEncoder.encode(CLAVE_ADMIN));
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        personRepository.save(admin);

        tokenAdmin = tokenAdmin();

        // Sedes de prueba reales en la base de datos: no se borran al final porque
        // los traslados creados en las pruebas quedan con una relacion (FK) hacia
        // ellas; borrarlas violaria esa relacion. Quedan como datos de prueba en
        // el entorno local, sin impacto real.
        sedeOrigenId = headquarterRepository.save(
                new HeadquarterDomain(null, "Sede Integracion Origen", "Cra 1 Test", true)).getId();
        sedeDestinoId = headquarterRepository.save(
                new HeadquarterDomain(null, "Sede Integracion Destino", "Cra 2 Test", true)).getId();
        sedeInactivaId = headquarterRepository.save(
                new HeadquarterDomain(null, "Sede Integracion Inactiva", "Cra 3 Test", false)).getId();

        ProductDTO producto = productService.create(
                new ProductDTO(
                        null,
                        "Producto Integracion Transfer",
                        "Producto para pruebas de transferencias",
                        null,
                        50000,
                        TaxCategory.STANDARD,
                        true,
                        true,
                        ProductCategory.GENERAL,
                        null,
                        null
                )
        );

        productoId = producto.getId();

        stockService.adjustStock(productoId, sedeOrigenId, 100);
    }

    @AfterAll
    void limpiar() {
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
        @DisplayName("no puede listar traslados")
        void noListaSinToken() throws Exception {
            assertThat(transfers("GET", "", null, null).statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("no puede crear un traslado")
        void noCreaSinToken() throws Exception {
            String body = trasladoJson(sedeOrigenId, sedeDestinoId, 10);
            assertThat(transfers("POST", "", body, null).statusCode()).isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("El administrador")
    class Admin {

        @Test
        @DisplayName("crea un traslado y queda en estado PENDING")
        void creaTraslado() throws Exception {
            HttpResponse<String> resp = transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeDestinoId, 10), tokenAdmin);

            assertThat(resp.statusCode()).isEqualTo(201);
            assertThat(resp.body()).contains("\"status\":\"PENDING\"");
            assertThat(resp.body()).contains("\"name\":\"Sede Integracion Origen\"");
        }

        @Test
        @DisplayName("no puede crear un traslado con la misma sede de origen y destino")
        void noPermiteOrigenIgualADestino() throws Exception {
            HttpResponse<String> resp = transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeOrigenId, 10), tokenAdmin);

            assertThat(resp.statusCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("no puede crear un traslado hacia una sede inactiva")
        void noPermiteSedeInactiva() throws Exception {
            HttpResponse<String> resp = transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeInactivaId, 10), tokenAdmin);

            assertThat(resp.statusCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("puede consultar por id el traslado que creo")
        void consultaPorId() throws Exception {
            String id = idDe(transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeDestinoId, 5), tokenAdmin));

            assertThat(transfers("GET", "/" + id, null, tokenAdmin).statusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("cancela un traslado pendiente y queda en CANCELLED")
        void cancelaTraslado() throws Exception {
            String id = idDe(transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeDestinoId, 5), tokenAdmin));

            assertThat(transfers("PATCH", "/" + id + "/cancel", null, tokenAdmin).statusCode()).isEqualTo(204);
            assertThat(transfers("GET", "/" + id, null, tokenAdmin).body())
                    .contains("\"status\":\"CANCELLED\"");
        }

        @Test
        @DisplayName("no puede editar un traslado que ya no esta pendiente")
        void noEditaSiNoEstaPendiente() throws Exception {
            String id = idDe(transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeDestinoId, 5), tokenAdmin));

            transfers("PATCH", "/" + id + "/status",
                    "{\"status\":\"IN_PROGRESS\"}", tokenAdmin);

            HttpResponse<String> resp = transfers("PUT", "/" + id,
                    trasladoJson(sedeOrigenId, sedeDestinoId, 20), tokenAdmin);

            assertThat(resp.statusCode()).isEqualTo(409);
        }

        @Test
        @DisplayName("no puede saltar de PENDING a COMPLETED directamente")
        void noSaltaEstados() throws Exception {
            String id = idDe(transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeDestinoId, 5), tokenAdmin));

            HttpResponse<String> resp = transfers("PATCH", "/" + id + "/status",
                    "{\"status\":\"COMPLETED\"}", tokenAdmin);

            assertThat(resp.statusCode()).isEqualTo(409);
        }

        @Test
        @DisplayName("filtra la lista de traslados por estado")
        void filtraPorEstado() throws Exception {
            transfers("POST", "",
                    trasladoJson(sedeOrigenId, sedeDestinoId, 5), tokenAdmin);

            HttpResponse<String> resp = transfers("GET", "?status=PENDING", null, tokenAdmin);

            assertThat(resp.statusCode()).isEqualTo(200);
            assertThat(resp.body()).contains("\"status\":\"PENDING\"");
        }
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private String tokenAdmin() throws Exception {
        HttpResponse<String> login = raw("POST", "/api/v1/persons/login",
                "{\"email\":\"" + ADMIN + "\",\"password\":\"" + CLAVE_ADMIN + "\"}", null);
        return campo(login.body(), "token");
    }

    private HttpResponse<String> transfers(String metodo, String ruta, String cuerpo, String token) throws Exception {
        return raw(metodo, "/api/transfers" + ruta, cuerpo, token);
    }

    private HttpResponse<String> raw(String metodo, String ruta, String cuerpo, String token) throws Exception {
        HttpRequest.BodyPublisher publicador = cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo);

        HttpRequest.Builder peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta))
                .method(metodo, publicador)
                .header("Content-Type", "application/json");

        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }

        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String idDe(HttpResponse<String> respuestaCreacion) {
        return campo(respuestaCreacion.body(), "id");
    }

    private String trasladoJson(UUID origen, UUID destino, int cantidad) {
        return """
                {"originHeadquarterId":"%s","destinationHeadquarterId":"%s","productId":"%s","quantity":%d,"observations":"Prueba de integracion"}
                """.formatted(origen, destino, productoId, cantidad);
    }

    private static String campo(String json, String nombre) {
        int desde = json.indexOf("\"" + nombre + "\":\"") + nombre.length() + 4;
        return json.substring(desde, json.indexOf('"', desde));
    }
}