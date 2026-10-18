package com.uco.ucopetapi.service.product;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CatalogValidationUtils {

    private CatalogValidationUtils() {
    }

    public static final String PRODUCT = "producto";
    public static final String SERVICE = "servicio";

    public static void requirePriceWhenSellable(Boolean sellable, Integer price, String item) {
        if (Boolean.TRUE.equals(sellable) && (price == null || price <= 0)) {
            throw new IllegalArgumentException("Un " + item + " vendible necesita un precio mayor a cero");
        }
    }

    public static void rejectPriceWhenNotSellable(Boolean sellable, Integer price, String item) {
        if (Boolean.FALSE.equals(sellable) && price != null) {
            throw new IllegalArgumentException(
                    "Un " + item + " que no se vende al público no puede llevar precio");
        }
    }

    public static void validatePriceOnUpdate(Integer price) {
        if (price != null && price <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
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