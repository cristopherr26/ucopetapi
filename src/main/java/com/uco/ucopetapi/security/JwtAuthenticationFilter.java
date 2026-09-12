package com.uco.ucopetapi.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.repository.person.PersonRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final PersonRepository personRepository;

    public JwtAuthenticationFilter(JwtService jwtService, PersonRepository personRepository) {
        this.jwtService = jwtService;
        this.personRepository = personRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.validar(cabecera.substring(7)).getPayload();

                Object bruto = claims.get("tv");
                Integer version = bruto instanceof Number n ? n.intValue() : null;
                boolean sigueValida = personRepository.findById(UUID.fromString(claims.getSubject()))
                        .map(persona -> persona.isActive()
                                && version != null
                                && version.intValue() == persona.getTokenVersion())
                        .orElse(false);
                if (!sigueValida) {
                    SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> permisos = roles == null ? List.of()
                        : roles.stream()
                                .map(Role::de)
                                .flatMap(Optional::stream)
                                .map(rol -> new SimpleGrantedAuthority(rol.authority()))
                                .toList();

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(claims.getSubject(), null, permisos));
            } catch (JwtException _) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
