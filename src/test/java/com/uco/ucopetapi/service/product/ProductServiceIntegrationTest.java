package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.IProductRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IStockRepository stockRepository;

    @Test
    void createProduct_isSavedByTheProcessorAndCanBeRetrieved() {
        ProductDTO created = productService.create(product(unique("Producto de prueba integración"), 50000));
        assertNotNull(created.getId());

        waitUntil(() -> productRepository.existsById(created.getId()), "el producto " + created.getId());
        ProductDTO fetched = productService.getById(created.getId(), null);
        assertEquals(created.getName(), fetched.getName());
        assertEquals(0, new BigDecimal("50000").compareTo(fetched.getPrice()));
    }

    @Test
    void createProduct_sellableWithoutPrice_isRejectedBeforePublishing() {
        ProductDTO withoutPrice = product(unique("Producto sin precio"), null);
        assertThrows(ResponseStatusException.class, () -> productService.create(withoutPrice));
    }

    @Test
    void createProduct_fromJavaWithoutName_isRejectedByTheDtoAnnotations() {
        ProductDTO withoutName = product(" ", 50000);
        ConstraintViolationException error = assertThrows(ConstraintViolationException.class,
                () -> productService.create(withoutName));
        assertTrue(error.getMessage().contains("El nombre es obligatorio"));
    }

    @Test
    void adjustStock_concurrentAdjustments_noLostUpdates() throws InterruptedException {
        HeadquarterDomain headquarter = headquarterRepository.save(
                new HeadquarterDomain(null, unique("Sede concurrencia"), "Direccion de prueba", true));
        ProductDTO created = productService.create(product(unique("Producto para concurrencia"), 10000));
        waitUntil(() -> productRepository.existsById(created.getId()), "el producto " + created.getId());
        stockService.adjustStock(created.getId(), headquarter.getId(), 100);
        waitUntil(() -> quantity(created.getId(), headquarter.getId()) == 100, "el stock inicial");

        int threads = 10;
        CountDownLatch latch = new CountDownLatch(threads);
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        stockService.adjustStock(created.getId(), headquarter.getId(), 1);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        waitUntil(() -> quantity(created.getId(), headquarter.getId()) == 110, "los 10 ajustes");
        StockDTO result = stockService.findByProductAndHeadquarter(created.getId(), headquarter.getId());
        assertEquals(110, result.getQuantity());
    }

    private int quantity(UUID productId, UUID headquarterId) {
        return stockRepository.findByProduct_IdAndHeadquarterId(productId, headquarterId)
                .map(StockDomain::getQuantity)
                .orElse(0);
    }

    private void waitUntil(BooleanSupplier condition, String what) {
        await().alias(what + " no aparecio en la base despues de 5 s. "
                        + "Esta corriendo RabbitMQ y el procesador de Inventario?")
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(condition::getAsBoolean);
    }

    private static ProductDTO product(String name, Integer price) {
        return new ProductDTO(null, name, "Descripción de prueba", null, price, TaxCategory.STANDARD,
                true, true, ProductCategory.GENERAL, null, null);
    }

    private static String unique(String name) {
        return name + " " + UUID.randomUUID().toString().substring(0, 8);
    }
}
