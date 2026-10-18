package com.uco.ucopetapi.controllers.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import com.uco.ucopetapi.repository.product.ICatalogItemRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("API del catalogo: Location y formato de error")
class CatalogApiIntegrationTest {

    private static final String PRODUCTS = "/api/v1/products";
    private static final String CONTENT_TYPE = "Content-Type";

    private static final String ADMIN = "admin@ucopet.com";
    private static final String ADMIN_PASSWORD = "claveCatalogoPrueba";

    @LocalServerPort
    private int port;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ICatalogItemRepository itemRepository;


    private final HttpClient http = HttpClient.newHttpClient();
    private final List<UUID> createdProducts = new ArrayList<>();
    private final List<UUID> createdServices = new ArrayList<>();
    private final List<UUID> createdHeadquarters = new ArrayList<>();
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private IStockRepository stockRepository;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    private Statistics statistics;

    private String originalAdminHash;
    private String token;

    @BeforeAll
    void login() throws Exception {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        originalAdminHash = admin.getPasswordHash();
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        personRepository.save(admin);
        token = field(call("POST", "/api/v1/persons/login",
                "{\"email\":\"" + ADMIN + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}").body(), "token");
    }

    @AfterAll
    void cleanUp() {
        PersonDomain admin = personRepository.findByEmail(ADMIN).orElseThrow();
        admin.setPasswordHash(originalAdminHash);
        personRepository.save(admin);
        for (UUID id : createdProducts) {
            stockRepository.deleteAll(stockRepository.findByItem_Id(id));
            itemRepository.deleteById(id);
        }
        itemRepository.deleteAllById(createdServices);
        headquarterRepository.deleteAllById(createdHeadquarters);
    }

    @Test
    @DisplayName("crear un producto responde 201 con un Location que lleva al producto")
    void createProduct_returnsLocation() throws Exception {
        String name = "Producto Location " + UUID.randomUUID();
        HttpResponse<String> created = call("POST", PRODUCTS,
                "{\"name\":\"" + name + "\",\"category\":\"GENERAL\",\"taxCategory\":\"STANDARD\","
                        + "\"sellable\":true,\"price\":1000}");
        assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
        String id = field(created.body(), "id");
        createdProducts.add(UUID.fromString(id));

        String location = created.headers().firstValue("Location").orElse("");
        assertThat(location).endsWith(PRODUCTS + "/" + id);

        HttpResponse<String> fetched = http.send(authorized(HttpRequest.newBuilder(URI.create(location)).GET()),
                HttpResponse.BodyHandlers.ofString());
        assertThat(fetched.statusCode()).isEqualTo(200);
        assertThat(field(fetched.body(), "name")).isEqualTo(name);
    }

    @Test
    @DisplayName("crear un servicio responde 201 con un Location que lleva al servicio")
    void createService_returnsLocation() throws Exception {
        HttpResponse<String> created = call("POST", "/api/v1/services",
                "{\"name\":\"Servicio Location " + UUID.randomUUID() + "\",\"category\":\"GENERAL\","
                        + "\"taxCategory\":\"STANDARD\",\"sellable\":true,\"price\":1000,\"purchasable\":false}");
        assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
        String id = field(created.body(), "id");
        createdServices.add(UUID.fromString(id));

        assertThat(created.headers().firstValue("Location").orElse("")).endsWith("/api/v1/services/" + id);
    }

    @Test
    @DisplayName("un dato invalido responde 400 en JSON, con la forma del resto de la API")
    void validationError_isTeamJson() throws Exception {
        HttpResponse<String> response = call("POST", PRODUCTS,
                "{\"name\":\"Sin precio " + UUID.randomUUID() + "\",\"category\":\"GENERAL\","
                        + "\"taxCategory\":\"STANDARD\",\"sellable\":true}");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue(CONTENT_TYPE).orElse("")).contains("application/json");
        assertThat(response.body()).contains("\"status\":400", "\"error\":\"Bad Request\"", "\"timestamp\"");
        assertThat(field(response.body(), "message")).isEqualTo("Un producto vendible necesita un precio mayor a cero");
    }

    @Test
    @DisplayName("un producto que no existe responde 404 en JSON")
    void notFound_isTeamJson() throws Exception {
        HttpResponse<String> response = call("GET", PRODUCTS + "/" + UUID.randomUUID(), null);

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.headers().firstValue(CONTENT_TYPE).orElse("")).contains("application/json");
        assertThat(response.body()).contains("\"status\":404", "\"error\":\"Not Found\"");
        assertThat(field(response.body(), "message")).startsWith("Producto no encontrado");
    }

    @Test
    @DisplayName("un enum invalido en el cuerpo responde 400 en JSON, no 500")
    void unreadableBody_isTeamJson() throws Exception {
        HttpResponse<String> response = call("POST", PRODUCTS,
                "{\"name\":\"Enum malo\",\"category\":\"NO_EXISTE\",\"taxCategory\":\"STANDARD\",\"sellable\":false}");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("\"status\":400", "\"message\"");
    }

    @Test
    @DisplayName("el PUT ya no da de baja ni reactiva: eso es del DELETE")
    void update_cannotChangeActive() throws Exception {
        String name = "Producto Active " + UUID.randomUUID();
        HttpResponse<String> created = call("POST", PRODUCTS,
                "{\"name\":\"" + name + "\",\"category\":\"GENERAL\",\"taxCategory\":\"STANDARD\","
                        + "\"sellable\":true,\"price\":1000}");
        String id = field(created.body(), "id");
        createdProducts.add(UUID.fromString(id));

        HttpResponse<String> ignored = call("PUT", PRODUCTS + "/" + id,
                "{\"active\":false,\"description\":\"sigue viva\"}");
        assertThat(ignored.statusCode()).as(ignored.body()).isEqualTo(200);
        assertThat(ignored.body()).contains("\"active\":true").contains("sigue viva");

        assertThat(call("DELETE", PRODUCTS + "/" + id, null).statusCode()).isEqualTo(204);
        assertThat(call("GET", PRODUCTS + "/" + id, null).body()).contains("\"active\":false");

        HttpResponse<String> notRevived = call("PUT", PRODUCTS + "/" + id, "{\"active\":true}");
        assertThat(notRevived.statusCode()).as(notRevived.body()).isEqualTo(200);
        assertThat(call("GET", PRODUCTS + "/" + id, null).body()).contains("\"active\":false");
    }

    @Test
    @DisplayName("listar con sede no dispara una consulta por producto")
    void listWithHeadquarter_doesNotQueryPerProduct() throws Exception {
        HttpResponse<String> headquarter = call("POST", "/api/v1/headquarter",
                "{\"name\":\"Sede N+1 " + System.currentTimeMillis() % 100000 + "\",\"address\":\"Calle 1\"}");
        assertThat(headquarter.statusCode()).as(headquarter.body()).isEqualTo(201);
        String headquarterId = field(headquarter.body(), "id");
        createdHeadquarters.add(UUID.fromString(headquarterId));

        for (int i = 0; i < 5; i++) {
            HttpResponse<String> created = call("POST", PRODUCTS,
                    "{\"name\":\"Producto N1 " + UUID.randomUUID() + "\",\"category\":\"GENERAL\","
                            + "\"taxCategory\":\"STANDARD\",\"sellable\":true,\"price\":1000}");
            String productId = field(created.body(), "id");
            createdProducts.add(UUID.fromString(productId));
            assertThat(call("POST", "/api/v1/stock/adjust",
                    "{\"productId\":\"" + productId + "\",\"headquarterId\":\"" + headquarterId
                            + "\",\"quantity\":" + (i + 1) + "}").statusCode()).isEqualTo(200);
        }

        statistics.clear();
        HttpResponse<String> listed = call("GET", "/api/v1/products?headquarterId=" + headquarterId, null);
        long queries = statistics.getPrepareStatementCount();

        assertThat(listed.statusCode()).as(listed.body()).isEqualTo(200);
        assertThat(queries)
                .as("el listado deberia costar lo mismo con 5 productos que con 500, pero costo " + queries)
                .isLessThanOrEqualTo(6);
    }

    @Test
    @DisplayName("el catalogo mixto lista productos y servicios juntos, por nombre y solo activos")
    void mixedCatalog_listsBothTypesSortedByName() throws Exception {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        String serviceId = field(call("POST", "/api/v1/services",
                "{\"name\":\"Mixto A " + tag + "\",\"category\":\"GENERAL\",\"taxCategory\":\"STANDARD\","
                        + "\"sellable\":true,\"price\":1000,\"purchasable\":false}").body(), "id");
        createdServices.add(UUID.fromString(serviceId));
        String productId = field(call("POST", PRODUCTS,
                "{\"name\":\"Mixto B " + tag + "\",\"category\":\"GENERAL\",\"taxCategory\":\"STANDARD\","
                        + "\"sellable\":true,\"price\":1000}").body(), "id");
        createdProducts.add(UUID.fromString(productId));
        String inactiveId = field(call("POST", PRODUCTS,
                "{\"name\":\"Mixto C " + tag + "\",\"category\":\"GENERAL\",\"taxCategory\":\"STANDARD\","
                        + "\"sellable\":false}").body(), "id");
        createdProducts.add(UUID.fromString(inactiveId));
        assertThat(call("DELETE", PRODUCTS + "/" + inactiveId, null).statusCode()).isEqualTo(204);

        HttpResponse<String> listed = call("GET", "/api/v1/catalog", null);
        assertThat(listed.statusCode()).as(listed.body()).isEqualTo(200);
        List<String> mine = new ArrayList<>();
        for (JsonNode item : new ObjectMapper().readTree(listed.body())) {
            if (item.get("name").asString().endsWith(tag)) {
                mine.add(item.get("id").asString() + ":" + item.get("type").asString());
            }
        }
        assertThat(mine).containsExactly(serviceId + ":SERVICE", productId + ":PRODUCT");

        assertThat(call("GET", "/api/v1/catalog?active=false", null).body())
                .contains(inactiveId)
                .doesNotContain(serviceId, productId);
    }

    private HttpResponse<String> call(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header(CONTENT_TYPE, "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        return http.send(authorized(request), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest authorized(HttpRequest.Builder request) {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request.build();
    }

    private static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
