// service/product/CatalogValidationUtils.java — versión completa
package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CatalogValidationUtils {

    private CatalogValidationUtils() {
    }

    public static void validateProviders(List<AssociatedSupplierDTO> providers) {
        if (providers == null) {
            return;
        }
        Set<UUID> seen = new HashSet<>();
        for (AssociatedSupplierDTO supplier : providers) {
            if (supplier.getProviderId() == null) {
                throw new IllegalArgumentException("Cada proveedor asociado necesita un providerId");
            }
            if (!seen.add(supplier.getProviderId())) {
                throw new IllegalArgumentException("No puedes asociar el mismo proveedor más de una vez en la misma petición");
            }
            if (supplier.getReferencePrice() != null && supplier.getReferencePrice() < 0) {
                throw new IllegalArgumentException("El precio de referencia no puede ser negativo");
            }
        }
    }

    public static void validateNoDuplicateHeadquarters(List<UUID> headquarterIds) {
        if (headquarterIds == null) {
            return;
        }
        Set<UUID> seen = new HashSet<>();
        for (UUID headquarterId : headquarterIds) {
            if (headquarterId == null) {
                throw new IllegalArgumentException("La lista de sedes no puede contener valores nulos");
            }
            if (!seen.add(headquarterId)) {
                throw new IllegalArgumentException("No puedes repetir la misma sede más de una vez");
            }
        }
    }
}