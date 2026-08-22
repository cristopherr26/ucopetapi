package com.uco.ucopetapi.controllers.auth;

import com.uco.ucopetapi.dto.auth.AccessTokenResponseDTO;
import com.uco.ucopetapi.dto.auth.AuthResponseDTO;
import com.uco.ucopetapi.dto.auth.BranchSwitchRequestDTO;
import com.uco.ucopetapi.dto.auth.ForgotPasswordRequestDTO;
import com.uco.ucopetapi.dto.auth.LoginRequestDTO;
import com.uco.ucopetapi.dto.auth.MeResponseDTO;
import com.uco.ucopetapi.dto.auth.ReauthenticateRequestDTO;
import com.uco.ucopetapi.dto.auth.RefreshResponseDTO;
import com.uco.ucopetapi.dto.auth.ResetPasswordRequestDTO;
import com.uco.ucopetapi.dto.auth.TokenValidationResponseDTO;
import com.uco.ucopetapi.dto.auth.TwoFactorResendRequestDTO;
import com.uco.ucopetapi.dto.auth.TwoFactorVerifyRequestDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Servicios 1 a 11 de identidad: sesion y contrasena. Sin implementar todavia.
@RestController
@RequestMapping("/auth")
public class AuthController {

    // 1 · Iniciar sesion
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        return null;
    }

    // 2 · Verificar el codigo de dos pasos
    @PostMapping("/two-factor/verify")
    public AuthResponseDTO verifyTwoFactor(@RequestBody TwoFactorVerifyRequestDTO request) {
        return null;
    }

    // 3 · Reenviar el codigo
    @PostMapping("/two-factor/resend")
    public void resendTwoFactor(@RequestBody TwoFactorResendRequestDTO request) {
    }

    // 4 · Renovar el access token. Sin cuerpo: el refresh viaja en la cookie.
    @PostMapping("/refresh")
    public RefreshResponseDTO refresh() {
        return null;
    }

    // 5 · Volver despues de la inactividad
    @PostMapping("/reauthenticate")
    public AccessTokenResponseDTO reauthenticate(@RequestBody ReauthenticateRequestDTO request) {
        return null;
    }

    // 6 · Cambiar la sede activa
    @PutMapping("/session/branch")
    public AccessTokenResponseDTO switchBranch(@RequestBody BranchSwitchRequestDTO request) {
        return null;
    }

    // 7 · Cerrar sesion
    @PostMapping("/logout")
    public void logout() {
    }

    // 8 · Mis datos y mis sedes
    @GetMapping("/me")
    public MeResponseDTO me() {
        return null;
    }

    // 9 · Pedir recuperacion
    @PostMapping("/password/forgot")
    public void forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
    }

    // 10 · Validar el enlace de recuperacion
    @GetMapping("/password/validate")
    public TokenValidationResponseDTO validatePasswordToken(@RequestParam String token) {
        return null;
    }

    // 11 · Guardar la contrasena nueva
    @PostMapping("/password/reset")
    public void resetPassword(@RequestBody ResetPasswordRequestDTO request) {
    }
}
