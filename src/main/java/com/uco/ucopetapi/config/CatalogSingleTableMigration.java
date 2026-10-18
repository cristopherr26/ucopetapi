package com.uco.ucopetapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Component
@Order(1)
public class CatalogSingleTableMigration implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogSingleTableMigration.class);
    private static final String SERVICES = "services";

    private final EntityManager entityManager;

    public CatalogSingleTableMigration(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        update("ALTER TABLE products ADD COLUMN IF NOT EXISTS type varchar(20) NOT NULL DEFAULT 'PRODUCT'");
        update("ALTER TABLE products ALTER COLUMN category DROP NOT NULL");
        int items = update("UPDATE products SET type = 'PRODUCT' WHERE type IS NULL");
        int services = tableExists(SERVICES) ? update("""
                INSERT INTO products (id, name, description, image_url, price, tax_category,
                                      sellable, active, category, purchasable, type)
                SELECT s.id, s.name, s.description, s.image_url, s.price, s.tax_category,
                       s.sellable, s.active, s.category, s.purchasable, 'SERVICE'
                FROM services s
                WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.id = s.id)
                """) : 0;
        int recoveredCategories = productsHasColumn("service_category")
                ? update("UPDATE products SET category = CAST(service_category AS varchar) "
                        + "WHERE category IS NULL AND service_category IS NOT NULL")
                : 0;
        update("ALTER TABLE products DROP COLUMN IF EXISTS service_category");

        dropForeignKeysToServices("service_providers");
        dropForeignKeysToServices("service_headquarters");

        if (services > 0 || items > 0 || recoveredCategories > 0) {
            LOG.info("=====================================================================");
            LOG.info("Catalogo unificado en una sola tabla:");
            LOG.info("   productos marcados como PRODUCT: {}", items);
            LOG.info("   servicios trasladados:           {}", services);
            LOG.info("   categorias rescatadas:            {}", recoveredCategories);
            LOG.info("");
            LOG.info("Las tablas services y service_providers quedan como respaldo, sin uso.");
            LOG.info("Se borran a mano cuando el cambio lleve una semana en pie.");
            LOG.info("=====================================================================");
        }
    }

    private boolean productsHasColumn(String column) {
        return !entityManager.createNativeQuery(
                        "SELECT 1 FROM information_schema.columns WHERE table_name = 'products' AND column_name = :c")
                .setParameter("c", column)
                .getResultList().isEmpty();
    }

    private boolean tableExists(String table) {
        return !entityManager.createNativeQuery(
                        "SELECT 1 FROM information_schema.tables WHERE table_name = :t")
                .setParameter("t", table)
                .getResultList().isEmpty();
    }

    private int update(String statement) {
        return entityManager.createNativeQuery(statement).executeUpdate();
    }

    private void dropForeignKeysToServices(String table) {
        if (!tableExists(table) || !tableExists(SERVICES)) {
            return;
        }
        var names = entityManager.createNativeQuery("""
                        SELECT conname FROM pg_constraint
                        WHERE conrelid = CAST(:t AS regclass)
                          AND confrelid = CAST(:r AS regclass)
                          AND contype = 'f'
                        """)
                .setParameter("t", table)
                .setParameter("r", SERVICES)
                .getResultList();
        for (Object name : names) {
            update("ALTER TABLE " + table + " DROP CONSTRAINT " + name);
            LOG.info("Se quito la llave foranea {} de {} hacia services", name, table);
        }
    }
}
