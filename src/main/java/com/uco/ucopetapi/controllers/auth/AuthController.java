package com.uco.ucopetapi.controllers.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uco.ucopetapi.dto.auth.LoginRequestDTO;
import com.uco.ucopetapi.dto.auth.LoginResponseDTO;
import com.uco.ucopetapi.services.auth.AuthService;

import jakarta.validation.Valid;

/** Entrada al sistema. */
@RestController
@RequestMapping("/api/v1/rest")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
