package com.uco.ucopetapi.controllers.auth;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uco.ucopetapi.dto.auth.LoginRequestDTO;
import com.uco.ucopetapi.dto.auth.LoginResponseDTO;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/rest")
public class AuthController {

    private static final UUID EJEMPLO = UUID.fromString("3f2a8c1e-4b5d-4e2a-9c11-77f1a0c4b9de");

    @PostMapping("/auth/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {

        return new LoginResponseDTO("token-de-ejemplo", EJEMPLO, "Ana Maria Rios", "MEDICO");
    }
}
