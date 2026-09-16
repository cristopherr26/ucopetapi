package com.uco.ucopetapi.purchases;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseItemRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.service.headquarter.HeadquarterService;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.provider.ProviderService;
import com.uco.ucopetapi.service.purchases.PurchaseService;
import com.uco.ucopetapi.service.purchases.exception.DuplicatePurchaseNumberException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Pruebas de integración del módulo de Compra (Purchase). Los datos de
 * referencia (proveedor, sede, producto) se crean en {@code @BeforeEach}
 * dentro de la misma transacción que hace rollback al final de cada test
 * — no dejan ningún rastro persistente en la base de datos real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
class PurchaseIntegrationTest {

    /** Fixture de ESTE módulo: proveedor de prueba, no compartido con otros archivos de test. */
    private static final UUID FIXED_SUPPLIER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** Fixture de ESTE módulo: sede de prueba, no compartida con otros archivos de test. */
    private static final UUID FIXED_HEADQUARTER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    /** Fixture de ESTE módulo: producto de prueba, no compartido con otros archivos de test. */
    private static final UUID FIXED_PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private HeadquarterService headquarterService;

    @Autowired
    private ProductService productService;

    @PersistenceContext
    private EntityManager entityManager;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Garantiza que existan los fixtures de proveedor, sede y producto con
     * sus ids fijos antes de cada test. Es idempotente: si el fixture ya
     * existe (por ejemplo, de una corrida anterior que no hizo rollback),
     * no lo recrea.
     *
     * <p>También autentica el hilo del test: {@code PurchaseServiceImpl}
     * llama al {@code PurchaseService} directamente (sin pasar por el
     * filtro JWT real, igual que {@code OrderIntegrationTest}), y
     * {@code currentPersonId()} exige un {@code Authentication} no nulo en
     * el {@code SecurityContextHolder}. No afecta a
     * {@code shouldAllowAdminToListPurchasesViaHttp()}: esa petición HTTP
     * corre en el hilo del servidor embebido, con su propio
     * {@code SecurityContext} resuelto por el filtro JWT real.
     */
    @BeforeEach
    void setUp() {
        ensureProviderFixture();
        ensureHeadquarterFixture();
        ensureProductFixture();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void ensureProviderFixture() {
        try {
            providerService.findById(FIXED_SUPPLIER_ID);
        } catch (NoSuchElementException notFound) {
            ProviderDTO created = providerService.create(new ProviderDTO(
                    FIXED_SUPPLIER_ID,
                    "PURCHASE TEST FIXTURE - Proveedor",
                    "PURCHASE TEST FIXTURE - Representante",
                    UUID.randomUUID(),
                    "PURCHASE-TEST-DOC",
                    "3000000000",
                    "Direccion de prueba",
                    "purchase.fixture@test.com",
                    true
            ));
            if (!FIXED_SUPPLIER_ID.equals(created.getId())) {
                fail("El id fijo no fue respetado al crear Proveedor, no se puede continuar con datos estables");
            }
        }
    }

    /**
     * HeadquarterDomain.id usa {@code @GeneratedValue(strategy = GenerationType.AUTO)}:
     * tanto {@code HeadquarterService.save()} (que delega en {@code JpaRepository.save()}
     * y hace {@code merge()} al ver un id no nulo, provocando un
     * {@code StaleObjectStateException} porque la fila todavía no existe) como
     * {@code EntityManager.persist()} directo (que lanza
     * {@code PersistentObjectException: entity already has an id assigned to it})
     * rechazan un id asignado manualmente. Se inserta con SQL nativo, que no pasa por
     * la generación de identificadores de Hibernate, para forzar el id fijo.
     */
    private void ensureHeadquarterFixture() {
        if (headquarterService.findById(FIXED_HEADQUARTER_ID).isPresent()) {
            return;
        }
        entityManager.createNativeQuery(
                        "INSERT INTO headquarter (id, name, address, is_active) "
                                + "VALUES (:id, :name, :address, :isActive)")
                .setParameter("id", FIXED_HEADQUARTER_ID)
                .setParameter("name", "PURCHASE TEST FIXTURE - Sede")
                .setParameter("address", "Direccion de prueba")
                .setParameter("isActive", true)
                .executeUpdate();
        entityManager.flush();

        if (headquarterService.findById(FIXED_HEADQUARTER_ID).isEmpty()) {
            fail("El insert nativo de Sede no dejo la fila esperada con el id fijo");
        }
    }

    /**
     * {@code ProductServiceImpl.create()} ignora por completo cualquier id recibido en
     * el {@code ProductDTO} y siempre genera uno nuevo ({@code UUID.randomUUID()}
     * hardcodeado en el código), así que nunca puede respetar el id fijo. Se inserta
     * con SQL nativo para forzar el id fijo.
     */
    private void ensureProductFixture() {
        if (productExists()) {
            return;
        }
        entityManager.createNativeQuery(
                        "INSERT INTO products (id, name, description, price, tax_category, sellable, active, category) "
                                + "VALUES (:id, :name, :description, :price, :taxCategory, :sellable, :active, :category)")
                .setParameter("id", FIXED_PRODUCT_ID)
                .setParameter("name", "PURCHASE TEST FIXTURE - Producto")
                .setParameter("description", "Producto de prueba para Purchase")
                .setParameter("price", 1000)
                .setParameter("taxCategory", TaxCategory.STANDARD.name())
                .setParameter("sellable", true)
                .setParameter("active", true)
                .setParameter("category", ProductCategory.GENERAL.name())
                .executeUpdate();
        entityManager.flush();

        if (!productExists()) {
            fail("El insert nativo de Producto no dejo la fila esperada con el id fijo");
        }
    }

    private boolean productExists() {
        try {
            productService.getById(FIXED_PRODUCT_ID, null);
            return true;
        } catch (NoSuchElementException notFound) {
            return false;
        }
    }

    /**
     * Camino feliz: crear una orden de compra con supplier, headquarter y
     * product válidos debe persistir correctamente, calcular
     * subtotal/total, y quedar en estado PENDING.
     */
    @Test
    void shouldCreatePurchaseSuccessfully() {
        BigDecimal unitPrice = new BigDecimal("1000");
        int quantity = 2;
        PurchaseRequestDTO request = buildRequest("TEST-" + UUID.randomUUID(), quantity, unitPrice);

        PurchaseResponseDTO response = purchaseService.createPurchase(request);

        BigDecimal expectedTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        assertNotNull(response.id());
        assertEquals(PurchaseStatus.PENDING, response.status());
        assertEquals(0, expectedTotal.compareTo(response.subtotal()));
        assertEquals(0, expectedTotal.compareTo(response.total()));
        assertEquals(FIXED_SUPPLIER_ID, response.supplier().id());
        assertEquals(FIXED_HEADQUARTER_ID, response.headquarter().id());
    }

    /**
     * Regla de negocio: no se puede crear una orden con un purchaseNumber
     * que ya existe.
     */
    @Test
    void shouldNotCreatePurchaseWithDuplicatePurchaseNumber() {
        String purchaseNumber = "TEST-DUPLICATE";
        purchaseService.createPurchase(buildRequest(purchaseNumber, 1, new BigDecimal("500")));
        entityManager.flush();

        PurchaseRequestDTO secondRequest = buildRequest(purchaseNumber, 1, new BigDecimal("500"));
        assertThrows(DuplicatePurchaseNumberException.class,
                () -> purchaseService.createPurchase(secondRequest));
    }

    /**
     * Prueba de integración de seguridad: un usuario ADMIN autenticado con
     * JWT real debe poder acceder a GET /purchases (protegido con
     * {@code @PreAuthorize hasRole ADMIN}). No depende de los fixtures de
     * supplier/headquarter/product — solo valida que la capa de seguridad
     * deja pasar la petición.
     */
    @Test
    void shouldAllowAdminToListPurchasesViaHttp() throws Exception {
        String loginBody = """
                {"email":"admin@ucopet.com","password":"UcopetAdmin2026*"}
                """;
        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/persons/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build();
        HttpResponse<String> loginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginResponse.statusCode(),
                "El login del admin sembrado debe responder 200; revisa las credenciales del admin de arranque si esto falla");

        String token = extractField(loginResponse.body(), "token");

        HttpRequest listRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/purchases?headquarterId=" + UUID.randomUUID()))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, listResponse.statusCode());
    }

    /** Extrae un valor de tipo string de un JSON plano, sin depender de una librería externa. */
    private static String extractField(String json, String name) {
        int from = json.indexOf("\"" + name + "\":\"") + name.length() + 4;
        return json.substring(from, json.indexOf('"', from));
    }

    private PurchaseRequestDTO buildRequest(String purchaseNumber, int quantity, BigDecimal unitPrice) {
        return new PurchaseRequestDTO(
                purchaseNumber,
                FIXED_SUPPLIER_ID,
                FIXED_HEADQUARTER_ID,
                false,
                List.of(new PurchaseItemRequestDTO(FIXED_PRODUCT_ID, quantity, unitPrice))
        );
    }
}
