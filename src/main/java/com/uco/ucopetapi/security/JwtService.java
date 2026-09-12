package com.uco.ucopetapi.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uco.ucopetapi.dto.person.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long minutos;

    public JwtService(@Value("${ucopet.jwt.secret:}") String secreto,
                      @Value("${ucopet.jwt.expiration-minutes:720}") long minutos) {
        this.minutos = minutos;
        if (secreto == null || secreto.isBlank()) {
            this.key = Jwts.SIG.HS256.key().build();
            LOG.warn("UCOPET_JWT_SECRET no esta definida. Se genero una clave temporal: "
                    + "los tokens emitidos ahora NO van a servir despues de reiniciar.");
        } else if (secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "UCOPET_JWT_SECRET debe tener al menos 32 caracteres para HS256.");
        } else {
            this.key = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String generar(UUID personId, String fullName, List<Role> roles, int tokenVersion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(personId.toString())
                .claim("name", fullName)
                .claim("roles", roles.stream().map(Role::name).toList())
                .claim("tv", tokenVersion)
                .issuedAt(java.util.Date.from(ahora))
                .expiration(java.util.Date.from(ahora.plusSeconds(minutos * 60)))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> validar(String token) throws JwtException {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
