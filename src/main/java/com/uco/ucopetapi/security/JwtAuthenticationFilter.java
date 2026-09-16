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
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.validate(header.substring(7)).getPayload();

                Object raw = claims.get("tv");
                Integer version = raw instanceof Number n ? n.intValue() : null;
                boolean stillValid = personRepository.findById(UUID.fromString(claims.getSubject()))
                        .map(person -> person.isActive()
                                && version != null
                                && version.intValue() == person.getTokenVersion())
                        .orElse(false);
                if (!stillValid) {
                    SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles == null ? List.of()
                        : roles.stream()
                                .map(Role::from)
                                .flatMap(Optional::stream)
                                .map(role -> new SimpleGrantedAuthority(role.authority()))
                                .toList();

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities));
            } catch (JwtException _) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
