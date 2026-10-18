package com.uco.ucopetapi.service.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.ICatalogItemRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;

@SpringBootTest
@DisplayName("Auditoria del catalogo: concurrencia")
class CatalogConcurrencyAuditTest {

    private static final int THREADS = 10;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ICatalogItemRepository itemRepository;

    @Autowired
    private IStockRepository stockRepository;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    private final List<UUID> createdProducts = Collections.synchronizedList(new ArrayList<>());
    private final List<UUID> createdHeadquarters = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (UUID productId : createdProducts) {
            stockRepository.deleteAll(stockRepository.findByItem_Id(productId));
            itemRepository.deleteById(productId);
        }
        headquarterRepository.deleteAllById(createdHeadquarters);
    }

    private static CatalogItemDTO product(String name) {
        return CatalogItemDTO.builder()
                .name(name)
                .description("auditoria")
                .price(1000)
                .taxCategory(TaxCategory.STANDARD)
                .sellable(true)
                .category(CatalogCategory.GENERAL)
                .build();
    }

    private static void runTogether(Runnable task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                start.await();
                task.run();
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Diez ajustes de +1 al mismo tiempo deberian sumar 10")
    void concurrentAdjustments_shouldNotLoseUpdates() throws InterruptedException {
        HeadquarterDomain hq = headquarterRepository.save(new HeadquarterDomain(null,
                "Sede concurrencia " + UUID.randomUUID().toString().substring(0, 8), "Calle 1", true));
        createdHeadquarters.add(hq.getId());
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, product("Concurrencia " + UUID.randomUUID()));
        createdProducts.add(created.getId());
        stockService.adjustStock(created.getId(), hq.getId(), 1);

        AtomicInteger succeeded = new AtomicInteger();
        runTogether(() -> {
            stockService.adjustStock(created.getId(), hq.getId(), 1);
            succeeded.incrementAndGet();
        });

        int finalQuantity = stockService.findByProductAndHeadquarter(created.getId(), hq.getId()).getQuantity();
        assertEquals(1 + succeeded.get(), finalQuantity,
                succeeded.get() + " ajustes confirmados, pero el stock quedo en " + finalQuantity);
    }

    @Test
    @DisplayName("Diez ajustes al mismo tiempo sobre un stock que NO existe todavia")
    void concurrentAdjustments_whenRowDoesNotExist() throws InterruptedException {
        HeadquarterDomain hq = headquarterRepository.save(new HeadquarterDomain(null,
                "Sede carrera " + UUID.randomUUID().toString().substring(0, 8), "Calle 1", true));
        createdHeadquarters.add(hq.getId());
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, product("Carrera " + UUID.randomUUID()));
        createdProducts.add(created.getId());

        AtomicInteger succeeded = new AtomicInteger();
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        runTogether(() -> {
            try {
                stockService.adjustStock(created.getId(), hq.getId(), 1);
                succeeded.incrementAndGet();
            } catch (RuntimeException e) {
                errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        });

        int finalQuantity = stockService.findByProductAndHeadquarter(created.getId(), hq.getId()).getQuantity();
        assertEquals(succeeded.get(), finalQuantity,
                succeeded.get() + " confirmados y el stock quedo en " + finalQuantity + ". Errores: " + errors);
        assertEquals(THREADS, succeeded.get(),
                "fallaron " + errors.size() + " de " + THREADS + ": " + errors);
    }

    @Test
    @DisplayName("Diez altas simultaneas con el mismo nombre deberian dejar un solo producto")
    void concurrentCreates_sameName_shouldKeepOne() throws InterruptedException {
        String name = "Nombre en carrera " + UUID.randomUUID();
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        runTogether(() -> {
            try {
                createdProducts.add(catalogService.create(CatalogItemType.PRODUCT, product(name)).getId());
                succeeded.incrementAndGet();
            } catch (IllegalArgumentException _) {
                rejected.incrementAndGet();
            }
        });

        assertEquals(1, succeeded.get(), "se crearon " + succeeded.get() + " productos con el mismo nombre ("
                + rejected.get() + " rechazados)");
    }
}
