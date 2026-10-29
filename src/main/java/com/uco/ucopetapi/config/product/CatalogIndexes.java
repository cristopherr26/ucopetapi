package com.uco.ucopetapi.config.product;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Component
public class CatalogIndexes implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogIndexes.class);

    private static final List<String> ENUM_CHECKS = List.of(
            "ALTER TABLE products DROP CONSTRAINT IF EXISTS products_category_check",
            "ALTER TABLE services DROP CONSTRAINT IF EXISTS services_category_check",
            "ALTER TABLE stock_movements DROP CONSTRAINT IF EXISTS stock_movements_reason_check");

    private static final List<String> STATEMENTS = List.of(
            "CREATE UNIQUE INDEX IF NOT EXISTS ux_product_name ON products (lower(name))",
            "CREATE UNIQUE INDEX IF NOT EXISTS ux_service_name ON services (lower(name))");

    private final EntityManager entityManager;

    public CatalogIndexes(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ENUM_CHECKS.forEach(statement -> entityManager.createNativeQuery(statement).executeUpdate());
        for (String statement : STATEMENTS) {
            try {
                entityManager.createNativeQuery(statement).executeUpdate();
            } catch (RuntimeException _) {
                LOG.warn("=====================================================================");
                LOG.warn("No se pudo crear el índice único del catálogo:");
                LOG.warn("   {}", statement);
                LOG.warn("Casi siempre es porque la base ya tiene nombres repetidos.");
                LOG.warn("Sin el índice, dos altas simultáneas con el mismo nombre pueden");
                LOG.warn("crear dos filas. Para verlos:");
                LOG.warn("   SELECT lower(name), count(*) FROM products GROUP BY 1 HAVING count(*) > 1; (o services)");
                LOG.warn("=====================================================================");
            }
        }
    }
}
