package com.uco.ucopetapi.dto.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequestDTO(

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 72,
              message = "La contrasena debe tener entre 8 y 72 caracteres")
        String password) {
}
