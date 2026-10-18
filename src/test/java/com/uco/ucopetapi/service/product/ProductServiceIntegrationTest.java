package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.ICatalogItemRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private StockService stockService;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @Autowired
    private ICatalogItemRepository itemRepository;

    @Autowired
    private IStockRepository stockRepository;

    @Test
    void createProduct_persistsAndCanBeRetrieved() {
        CatalogItemDTO request = CatalogItemDTO.builder()
                .name("Producto de prueba integración")
                .description("Descripción de prueba")
                .price(50000)
                .taxCategory(TaxCategory.STANDARD)
                .sellable(true)
                .category(CatalogCategory.GENERAL)
                .build();

        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, request);
        assertNotNull(created.getId());

        CatalogItemDTO fetched = catalogService.getById(CatalogItemType.PRODUCT, created.getId(), null);
        assertEquals("Producto de prueba integración", fetched.getName());
        assertEquals(50000, fetched.getPrice());
    }

    @Test
    void createProduct_sellableWithoutPrice_throwsValidationError() {
        CatalogItemDTO request = CatalogItemDTO.builder()
                .name("Producto sin precio")
                .description("Descripción")
                .taxCategory(TaxCategory.STANDARD)
                .sellable(true)
                .category(CatalogCategory.GENERAL)
                .build();

        assertThrows(IllegalArgumentException.class, () -> catalogService.create(CatalogItemType.PRODUCT, request));
    }

    @Test
    void adjustStock_multipleAdjustments_accumulatesCorrectly() {
        HeadquarterDomain headquarter = headquarterRepository.save(
                new HeadquarterDomain(null, "Sede de prueba integración", "Dirección de prueba", true)
        );

        CatalogItemDTO request = CatalogItemDTO.builder()
                .name("Producto para stock")
                .description("Descripción")
                .price(30000)
                .taxCategory(TaxCategory.STANDARD)
                .sellable(true)
                .category(CatalogCategory.GENERAL)
                .build();
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, request);

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
        CatalogItemDTO request = CatalogItemDTO.builder()
                .name("Producto para concurrencia " + UUID.randomUUID())
                .description("Descripcion")
                .price(10000)
                .taxCategory(TaxCategory.STANDARD)
                .sellable(true)
                .category(CatalogCategory.GENERAL)
                .build();
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, request);
        try {
            stockService.adjustStock(created.getId(), headquarter.getId(), 100);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

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
            executor.shutdown();

            StockDTO result = stockService.findByProductAndHeadquarter(created.getId(), headquarter.getId());
            assertEquals(110, result.getQuantity());
        } finally {
            stockRepository.deleteAll(stockRepository.findByItem_Id(created.getId()));
            itemRepository.deleteById(created.getId());
            headquarterRepository.deleteById(headquarter.getId());
        }
    }
}