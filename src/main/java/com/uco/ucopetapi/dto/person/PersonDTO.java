package com.uco.ucopetapi.dto.person;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Una persona registrada en la clinica: personal o tutor.
 *
 * <p>El correo va aca porque es un dato de contacto de la persona, igual que el
 * telefono. La contrasena NO: esa es una credencial y no viaja en este DTO.
 */
public record PersonDTO(
        UUID id,

        @NotBlank(message = "El tipo de documento es obligatorio")
        String documentType,

        @NotBlank(message = "El numero de documento es obligatorio")
        String documentNumber,

        @NotBlank(message = "El nombre es obligatorio")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        @Email(message = "El correo no tiene un formato valido")
        String email,

        String phone,

        boolean active) {
}
