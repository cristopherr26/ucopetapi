package com.uco.ucopetapi.config;

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

    private static final List<String> STATEMENTS = List.of(
            "CREATE UNIQUE INDEX IF NOT EXISTS ux_products_lower_name ON products (lower(name))",
            "CREATE UNIQUE INDEX IF NOT EXISTS ux_services_lower_name ON services (lower(name))");

    private final EntityManager entityManager;

    public CatalogIndexes(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (String statement : STATEMENTS) {
            try {
                entityManager.createNativeQuery(statement).executeUpdate();
            } catch (RuntimeException duplicatesInThisDatabase) {
                LOG.warn("=====================================================================");
                LOG.warn("No se pudo crear el indice unico del catalogo:");
                LOG.warn("   {}", statement);
                LOG.warn("Casi siempre es porque la base ya tiene nombres repetidos.");
                LOG.warn("Sin el indice, dos altas simultaneas con el mismo nombre pueden");
                LOG.warn("crear dos filas. Para verlos:");
                LOG.warn("   SELECT lower(name), count(*) FROM products GROUP BY 1 HAVING count(*) > 1;");
                LOG.warn("=====================================================================");
            }
        }
    }
}
