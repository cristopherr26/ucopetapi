// src/test/java/com/uco/ucopetapi/service/product/ProductServiceIntegrationTest.java
package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @Test
    void createProduct_persistsAndCanBeRetrieved() {
        ProductDTO request = new ProductDTO(
                null, "Producto de prueba integración", "Descripción de prueba", null,
                50000, TaxCategory.STANDARD, true, true, ProductCategory.GENERAL, null, null
        );

        ProductDTO created = productService.create(request);
        assertNotNull(created.getId());

        ProductDTO fetched = productService.getById(created.getId(), null);
        assertEquals("Producto de prueba integración", fetched.getName());
        assertEquals(50000, fetched.getPrice());
    }

    @Test
    void createProduct_sellableWithoutPrice_throwsValidationError() {
        ProductDTO request = new ProductDTO(
                null, "Producto sin precio", "Descripción", null,
                null, TaxCategory.STANDARD, true, true, ProductCategory.GENERAL, null, null
        );

        assertThrows(IllegalArgumentException.class, () -> productService.create(request));
    }

    @Test
    void adjustStock_multipleAdjustments_accumulatesCorrectly() {
        HeadquarterDomain headquarter = headquarterRepository.save(
                new HeadquarterDomain(null, "Sede de prueba integración", "Dirección de prueba", true)
        );

        ProductDTO request = new ProductDTO(
                null, "Producto para stock", "Descripción", null,
                30000, TaxCategory.STANDARD, true, true, ProductCategory.GENERAL, null, null
        );
        ProductDTO created = productService.create(request);

        stockService.adjustStock(created.getId(), headquarter.getId(), 20);
        StockDTO afterSecond = stockService.adjustStock(created.getId(), headquarter.getId(), 15);

        assertEquals(35, afterSecond.getQuantity());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void adjustStock_concurrentAdjustments_noLostUpdates() throws InterruptedException {
        HeadquarterDomain headquarter = headquarterRepository.save(
                new HeadquarterDomain(null, "Sede concurrencia", "Direccion de prueba", true)
        );
        ProductDTO request = new ProductDTO(
                null, "Producto para concurrencia", "Descripcion", null,
                10000, TaxCategory.STANDARD, true, true, ProductCategory.GENERAL, null, null
        );
        ProductDTO created = productService.create(request);
        stockService.adjustStock(created.getId(), headquarter.getId(), 100);

        int hilos = 10;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch latch = new CountDownLatch(hilos);

        for (int i = 0; i < hilos; i++) {
            executor.submit(() -> {
                try {
                    stockService.adjustStock(created.getId(), headquarter.getId(), 1);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        StockDTO resultado = stockService.findByProductAndHeadquarter(created.getId(), headquarter.getId());
        assertEquals(110, resultado.getQuantity());
    }
}