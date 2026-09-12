package com.uco.ucopetapi.dto.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(

        @NotBlank(message = "La contrasena actual es obligatoria")
        @Size(max = 72, message = "La contrasena no puede pasar de 72 caracteres")
        String currentPassword,

        @NotBlank(message = "La contrasena nueva es obligatoria")
        @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
        String newPassword,

        @NotBlank(message = "Hay que confirmar la contrasena nueva")
        @Size(max = 72, message = "La contrasena no puede pasar de 72 caracteres")
        String confirmPassword) {
}
