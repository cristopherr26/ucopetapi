package com.uco.ucopetapi.service.product;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;

import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogItemDTO;

import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;

@SpringBootTest
@Transactional
@DisplayName("Auditoria del catalogo: reglas de negocio")
class CatalogRulesAuditTest {

    private static final String AUDITORIA = "auditoria";

    @Autowired
    private CatalogService catalogService;


    @Autowired
    private StockService stockService;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    private static String unique(String base) {
        return base + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    private CatalogItemDTO product(String name, Integer price, boolean sellable) {
        return CatalogItemDTO.builder()
                .name(name)
                .description(AUDITORIA)
                .price(price == null ? null : BigDecimal.valueOf(price))
                .taxCategory(TaxCategory.STANDARD)
                .sellable(sellable)
                .category(CatalogCategory.GENERAL)
                .build();
    }

    private CatalogItemDTO patch(String name, Boolean sellable) {
        return CatalogItemDTO.builder()
                .name(name)
                .sellable(sellable)
                .build();
    }

    private HeadquarterDomain headquarter() {
        return headquarterRepository.save(
                new HeadquarterDomain(null, unique("Sede auditoria"), "Calle 1", true));
    }

    private CatalogItemDTO service(String name, Boolean purchasable) {
        return CatalogItemDTO.builder()
                .name(name)
                .description(AUDITORIA)
                .price(BigDecimal.valueOf(15000))
                .taxCategory(TaxCategory.STANDARD)
                .sellable(true)
                .category(CatalogCategory.GENERAL)
                .purchasable(purchasable)
                .build();
    }

    @Test
    @DisplayName("Regla 1: sellable=false con precio se rechaza, ya no se descarta en silencio")
    void notSellable_withPrice_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> catalogService.create(CatalogItemType.PRODUCT, product(unique("No vendible"), 24000, false)));

        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, product(unique("No vendible"), null, false));
        assertNull(created.getPrice());
    }

    @Test
    @DisplayName("Regla 3: el precio se valida sobre el estado final, no sobre la peticion")
    void update_toSellableWithoutPrice_isRejected() {
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, product(unique("Pasa a vendible"), null, false));

        assertThrows(IllegalArgumentException.class,
                () -> catalogService.update(CatalogItemType.PRODUCT, created.getId(), patch(null, true)));
    }

    @Test
    @DisplayName("Regla 4: el nombre es unico sin distinguir mayusculas")
    void duplicateName_ignoringCase_isRejected() {
        String name = unique("Collar auditoria");
        catalogService.create(CatalogItemType.PRODUCT, product(name, 1000, true));

        assertThrows(DuplicateCatalogNameException.class,
                () -> catalogService.create(CatalogItemType.PRODUCT, product(name.toUpperCase(), 1000, true)));
    }

    @Test
    @DisplayName("Regla 5a: un servicio tiene que decir si es comprable a un tercero")
    void service_withoutPurchasable_isRejected() {
        CatalogItemDTO request = service(unique("Servicio sin purchasable"), null);

        assertThrows(IllegalArgumentException.class, () -> catalogService.create(CatalogItemType.SERVICE, request));
    }

    @Test
    @DisplayName("Regla 5b: purchasable se guarda y se puede cambiar al editar")
    void service_purchasable_canChange() {
        CatalogItemDTO created = catalogService.create(CatalogItemType.SERVICE, service(unique("Servicio externo"), true));
        assertTrue(created.getPurchasable());

        CatalogItemDTO updated = catalogService.update(CatalogItemType.SERVICE, created.getId(),
                CatalogItemDTO.builder().purchasable(false).build());

        assertEquals(Boolean.FALSE, updated.getPurchasable());
    }

    @Test
    @DisplayName("Stock: restar mas de lo que hay se rechaza y no cambia la cantidad")
    void adjustStock_belowZero_isRejected() {
        HeadquarterDomain hq = headquarter();
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, product(unique("Stock negativo"), 1000, true));
        stockService.adjustStock(created.getId(), hq.getId(), 20);

        assertThrows(IllegalStateException.class,
                () -> stockService.adjustStock(created.getId(), hq.getId(), -24));
        assertEquals(20, stockService.findByProductAndHeadquarter(created.getId(), hq.getId()).getQuantity());
    }

    @Test
    @DisplayName("Stock: restar donde no hay fila se rechaza")
    void adjustStock_subtractWithoutRow_isRejected() {
        HeadquarterDomain hq = headquarter();
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, product(unique("Sin fila"), 1000, true));

        assertThrows(IllegalStateException.class,
                () -> stockService.adjustStock(created.getId(), hq.getId(), -5));
    }

    @Test
    @DisplayName("Stock: consultar sin fila devuelve 0, no error")
    void stockWithoutRow_isZero() {
        HeadquarterDomain hq = headquarter();
        CatalogItemDTO created = catalogService.create(CatalogItemType.PRODUCT, product(unique("Stock cero"), 1000, true));

        assertEquals(0, stockService.findByProductAndHeadquarter(created.getId(), hq.getId()).getQuantity());
    }

    @Test
    @DisplayName("Refactor 6489409: PUT a un id inexistente deberia ser 404 aunque el nombre choque")
    void update_missingId_withTakenName_shouldBeNotFound() {
        String taken = unique("Nombre ocupado");
        catalogService.create(CatalogItemType.PRODUCT, product(taken, 1000, true));

        assertThrows(NoSuchElementException.class,
                () -> catalogService.update(CatalogItemType.PRODUCT, UUID.randomUUID(), patch(taken, null)));
    }
}
