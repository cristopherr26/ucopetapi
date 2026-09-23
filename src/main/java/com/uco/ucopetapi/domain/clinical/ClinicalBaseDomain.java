package com.uco.ucopetapi.domain.clinical;

import com.uco.ucopetapi.crosscutting.helpers.ObjectHelper;
import com.uco.ucopetapi.crosscutting.helpers.TextHelper;
import com.uco.ucopetapi.exception.clinical.ClinicalException;

import java.util.Collection;

/**
 * Clase base de los Domain del módulo clínico.
 * Regla del curso: las validaciones viven en el Domain; cada Domain la extiende
 * e implementa validate() con sus reglas.
 */
public abstract class ClinicalBaseDomain {

    /** Valida los datos propios de la entidad. Lanza ClinicalException (400) si algo no cumple. */
    public abstract void validate();

    protected static void requireNotNull(final Object value, final String message) {
        if (ObjectHelper.isNull(value)) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requireNotBlank(final String value, final String message) {
        if (TextHelper.getDefaultWithTrim(value).isEmpty()) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requireMaxLength(final String value, final int max, final String message) {
        if (!ObjectHelper.isNull(value) && value.length() > max) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requirePositive(final Integer value, final String message) {
        if (ObjectHelper.isNull(value) || value <= 0) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requirePositiveIfPresent(final Integer value, final String message) {
        if (!ObjectHelper.isNull(value) && value <= 0) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requireRange(final Number value, final double min, final double max, final String message) {
        if (ObjectHelper.isNull(value)) {
            return;
        }
        double number = value.doubleValue();
        if (number < min || number > max) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requireNotEmpty(final Collection<?> values, final String message) {
        if (ObjectHelper.isNull(values) || values.isEmpty()) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requireMaxSize(final Collection<?> values, final int max, final String message) {
        if (!ObjectHelper.isNull(values) && values.size() > max) {
            throw ClinicalException.badRequest(message);
        }
    }

    protected static void requireState(final boolean condition, final String message) {
        if (!condition) {
            throw ClinicalException.conflict(message);
        }
    }

    protected static void requireAuthor(final boolean condition, final String message) {
        if (!condition) {
            throw ClinicalException.forbidden(message);
        }
    }
}
