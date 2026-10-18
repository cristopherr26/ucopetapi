package com.uco.ucopetapi.service.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.repository.product.ICatalogItemRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Auditoria del catalogo: el JSON de verdad, por HTTP")
class CatalogHttpAuditTest {

    private static final String PRODUCTS = "/api/v1/products";

    private static final String ADMIN = "admin@ucopet.com";
    private static final String ADMIN_PASSWORD = "claveAuditoriaCatalogo";

    @LocalServerPort
    private int port;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ICatalogItemRepository itemRepository;

    private final HttpClient http = HttpClient.newHttpClient();
    private String originalAdminHash;
    private String token;
    private String productId;

    @BeforeAll
    void login() throws Exception {
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        originalAdminHash = admin.getPasswordHash();
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        personRepository.save(admin);

        HttpResponse<String> response = call("POST", "/api/v1/persons/login",
                "{\"email\":\"" + ADMIN + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}", null);
        token = field(response.body(), "token");
    }

    @AfterAll
    void restore() {
        if (productId != null) {
            UUID id = UUID.fromString(productId);
            itemRepository.deleteById(id);
        }
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        admin.setPasswordHash(originalAdminHash);
        personRepository.save(admin);
    }

    private HttpResponse<String> call(String method, String path, String body, String bearer) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (bearer != null) {
            request.header("Authorization", "Bearer " + bearer);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"?([^\",}]*)").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    @Test
    @DisplayName("POST con JSON crea el producto, y un PUT parcial conserva lo que no se mando")
    void jsonRoundTrip() throws Exception {
        String name = "Producto HTTP " + UUID.randomUUID();
        HttpResponse<String> created = call("POST", PRODUCTS,
                "{\"name\":\"" + name + "\",\"category\":\"GENERAL\",\"taxCategory\":\"REDUCED\","
                        + "\"sellable\":true,\"price\":80000}", token);
        assertEquals(201, created.statusCode(), created.body());
        productId = field(created.body(), "id");

        HttpResponse<String> updated = call("PUT", PRODUCTS + "/" + productId,
                "{\"price\":74000}", token);
        assertEquals(200, updated.statusCode(), updated.body());
        assertEquals("74000", field(updated.body(), "price"));
        assertEquals("REDUCED", field(updated.body(), "taxCategory"));
        assertEquals(name, field(updated.body(), "name"));
    }

    @Test
    @DisplayName("Un error de validacion responde 400 con cuerpo JSON, como el resto de la API")
    void validationError_isJson() throws Exception {
        HttpResponse<String> response = call("POST", PRODUCTS,
                "{\"name\":\"Sin precio " + UUID.randomUUID() + "\",\"category\":\"GENERAL\","
                        + "\"taxCategory\":\"STANDARD\",\"sellable\":true}", token);

        assertEquals(400, response.statusCode());
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.contains("application/json"),
                "Content-Type fue '" + contentType + "', cuerpo: " + response.body());
    }

    @Test
    @DisplayName("Un enum invalido en el JSON responde 400, no 500")
    void invalidEnum_isBadRequest() throws Exception {
        HttpResponse<String> response = call("POST", PRODUCTS,
                "{\"name\":\"Enum malo\",\"category\":\"NO_EXISTE\",\"taxCategory\":\"STANDARD\","
                        + "\"sellable\":false}", token);

        assertEquals(400, response.statusCode(), response.body());
    }
}
