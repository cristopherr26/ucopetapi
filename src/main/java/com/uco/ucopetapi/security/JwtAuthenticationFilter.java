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
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final PersonRepository personRepository;
    private final AuthCookie authCookie;

    public JwtAuthenticationFilter(JwtService jwtService, PersonRepository personRepository,
                                   AuthCookie authCookie) {
        this.jwtService = jwtService;
        this.personRepository = personRepository;
        this.authCookie = authCookie;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Optional<String> token = tokenOf(request);

        if (token.isPresent()) {
            try {
                Claims claims = jwtService.validate(token.get()).getPayload();

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
            } catch (JwtException | IllegalArgumentException _) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private Optional<String> tokenOf(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            return Optional.of(header.substring(BEARER.length()));
        }
        return authCookie.read(request);
    }
}
