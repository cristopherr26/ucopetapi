package com.uco.ucopetapi.dto.person;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonDTO(
        UUID id,

        @NotNull(message = "El tipo de documento es obligatorio")
        DocumentType documentType,

        @NotBlank(message = "El numero de documento es obligatorio")
        @Pattern(regexp = "[A-Za-z0-9-]{4,20}",
                 message = "El numero de documento solo admite letras, numeros y guiones")
        String documentNumber,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede pasar de 60 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 60, message = "El apellido no puede pasar de 60 caracteres")
        String lastName,

        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 120, message = "El correo no puede pasar de 120 caracteres")
        String email,

        @Size(max = 200, message = "La direccion no puede pasar de 200 caracteres")
        String address,

        @Size(max = 20, message = "El telefono no puede pasar de 20 caracteres")
        @Pattern(regexp = "\\+?[0-9 -]{7,20}|",
                 message = "El telefono solo admite numeros, espacios, guiones y un + inicial")
        String phone,

        boolean admin,

        boolean active,

        Instant deactivatedAt) {
}
