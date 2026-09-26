package com.uco.ucopetapi.service.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.IServiceProviderRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;

@SpringBootTest
@Transactional
@DisplayName("Auditoria del catalogo: reglas de negocio")
class CatalogRulesAuditTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private StockService stockService;

    @Autowired
    private HeadquarterRepository headquarterRepository;

    @Autowired
    private ProviderJPARepository providerRepository;

    @Autowired
    private IServiceProviderRepository serviceProviderRepository;

    private static String unique(String base) {
        return base + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    private ProductDTO product(String name, Integer price, boolean sellable) {
        return new ProductDTO(null, name, "auditoria", null, price, TaxCategory.STANDARD,
                sellable, true, ProductCategory.GENERAL, null, null);
    }

    private ProductDTO patch(String name, Boolean sellable) {
        return new ProductDTO(null, name, null, null, null, null, sellable, null, null, null, null);
    }

    private HeadquarterDomain headquarter() {
        return headquarterRepository.save(
                new HeadquarterDomain(null, unique("Sede auditoria"), "Calle 1", true));
    }

    private ProviderDomain provider() {
        return providerRepository.save(new ProviderDomain(UUID.randomUUID(), unique("Proveedor"),
                "Representante", UUID.randomUUID(), "900" + System.nanoTime() % 1000000,
                "3000000000", "Calle 2", "prov@ucopet.com", true));
    }

    @Test
    @DisplayName("Regla 1: sellable=false con precio se rechaza, ya no se descarta en silencio")
    void notSellable_withPrice_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.create(product(unique("No vendible"), 24000, false)));

        ProductDTO created = productService.create(product(unique("No vendible"), null, false));
        assertNull(created.getPrice());
    }

    @Test
    @DisplayName("Regla 3: el precio se valida sobre el estado final, no sobre la peticion")
    void update_toSellableWithoutPrice_isRejected() {
        ProductDTO created = productService.create(product(unique("Pasa a vendible"), null, false));

        assertThrows(IllegalArgumentException.class,
                () -> productService.update(created.getId(), patch(null, true)));
    }

    @Test
    @DisplayName("Regla 4: el nombre es unico sin distinguir mayusculas")
    void duplicateName_ignoringCase_isRejected() {
        String name = unique("Collar auditoria");
        productService.create(product(name, 1000, true));

        assertThrows(DuplicateCatalogNameException.class,
                () -> productService.create(product(name.toUpperCase(), 1000, true)));
    }

    @Test
    @DisplayName("Regla 5a: servicio no comprable no acepta proveedores al crear")
    void notPurchasableService_withProviders_isRejected() {
        ProviderDomain provider = provider();
        ServiceDTO request = new ServiceDTO(null, unique("Servicio propio"), "auditoria", null,
                15000, TaxCategory.STANDARD, true, true, ServiceCategory.GENERAL, false,
                List.of(new AssociatedSupplierDTO(provider.getId(), 10000)), null);

        assertThrows(IllegalArgumentException.class, () -> serviceService.create(request));
    }

    @Test
    @DisplayName("Regla 5b: pasar un servicio a no comprable borra sus proveedores")
    void serviceBecomesNotPurchasable_providersAreRemoved() {
        ProviderDomain provider = provider();
        ServiceDTO created = serviceService.create(new ServiceDTO(null, unique("Servicio externo"),
                "auditoria", null, 15000, TaxCategory.STANDARD, true, true, ServiceCategory.GENERAL,
                true, List.of(new AssociatedSupplierDTO(provider.getId(), 10000)), null));
        assertEquals(1, serviceProviderRepository.findByService_Id(created.getId()).size());

        serviceService.update(created.getId(), new ServiceDTO(null, null, null, null, null, null,
                null, null, null, false, null, null));

        assertTrue(serviceProviderRepository.findByService_Id(created.getId()).isEmpty());
    }

    @Test
    @DisplayName("Stock: restar mas de lo que hay se rechaza y no cambia la cantidad")
    void adjustStock_belowZero_isRejected() {
        HeadquarterDomain hq = headquarter();
        ProductDTO created = productService.create(product(unique("Stock negativo"), 1000, true));
        stockService.adjustStock(created.getId(), hq.getId(), 20);

        assertThrows(IllegalStateException.class,
                () -> stockService.adjustStock(created.getId(), hq.getId(), -24));
        assertEquals(20, stockService.findByProductAndHeadquarter(created.getId(), hq.getId()).getQuantity());
    }

    @Test
    @DisplayName("Stock: restar donde no hay fila se rechaza")
    void adjustStock_subtractWithoutRow_isRejected() {
        HeadquarterDomain hq = headquarter();
        ProductDTO created = productService.create(product(unique("Sin fila"), 1000, true));

        assertThrows(IllegalStateException.class,
                () -> stockService.adjustStock(created.getId(), hq.getId(), -5));
    }

    @Test
    @DisplayName("Stock: consultar sin fila devuelve 0, no error")
    void stockWithoutRow_isZero() {
        HeadquarterDomain hq = headquarter();
        ProductDTO created = productService.create(product(unique("Stock cero"), 1000, true));

        assertEquals(0, stockService.findByProductAndHeadquarter(created.getId(), hq.getId()).getQuantity());
    }

    @Test
    @DisplayName("Refactor 6489409: PUT a un id inexistente deberia ser 404 aunque el nombre choque")
    void update_missingId_withTakenName_shouldBeNotFound() {
        String taken = unique("Nombre ocupado");
        productService.create(product(taken, 1000, true));

        assertThrows(NoSuchElementException.class,
                () -> productService.update(UUID.randomUUID(), patch(taken, null)));
    }
}
