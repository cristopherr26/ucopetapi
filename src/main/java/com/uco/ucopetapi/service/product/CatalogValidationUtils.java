package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.dto.product.ProcessorReplyDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CatalogValidationUtils {

    private CatalogValidationUtils() {
    }

    public static void validatePrice(Boolean sellable, Integer price, String item) {
        if (Boolean.TRUE.equals(sellable) && (price == null || price <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un " + item + " vendible necesita un precio mayor a cero");
        }
        if (!Boolean.TRUE.equals(sellable) && price != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un " + item + " no vendible no puede llevar precio");
        }
    }

    public static void requireSaved(ProcessorReplyDTO reply, String item) {
        requireApplied(reply, "el " + item + " no se guardó");
    }

    public static void requireDeactivated(ProcessorReplyDTO reply, String item) {
        requireApplied(reply, "el " + item + " no se dio de baja");
    }

    private static void requireApplied(ProcessorReplyDTO reply, String notApplied) {
        if (reply == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El procesador de inventario no respondió a tiempo: " + notApplied);
        }
        if (reply.duplicateName()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, reply.reason());
        }
        if (!reply.applied()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reply.reason());
        }
    }

    public static void validateNoDuplicateHeadquarters(List<UUID> headquarterIds) {
        Set<UUID> seen = new HashSet<>();
        for (UUID headquarterId : headquarterIds) {
            if (headquarterId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La lista de sedes no puede contener valores nulos");
            }
            if (!seen.add(headquarterId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La misma sede no puede aparecer más de una vez");
            }
        }
    }
}
