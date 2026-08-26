package com.uco.ucopetapi.dto.auth;

import java.util.UUID;

/**
 * Lo que devuelve el login.
 *
 * <p>No lleva la contrasena ni su hash: seria exponer una credencial en la
 * respuesta. Por eso el login devuelve este DTO y no la persona del dominio.
 */
public record LoginResponseDTO(
        String token,
        UUID personId,
        String fullName,
        String role) {
}
