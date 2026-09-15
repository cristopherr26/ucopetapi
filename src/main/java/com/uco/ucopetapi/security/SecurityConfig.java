package com.uco.ucopetapi.security;

import jakarta.servlet.DispatcherType;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.uco.ucopetapi.dto.person.Role;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String PERSONS = "/api/v1/persons";
    private static final String PERSONS_ALL = PERSONS + "/**";

    private static final String[] USERS = {
        "/api/v1/doctor", "/api/v1/doctor/**",
        "/api/v1/pets", "/api/v1/pets/**",
        "/api/v1/tutorPet", "/api/v1/tutorPet/**"
    };

    private static final String[] NOTIFICATIONS = {
        "/api/notifications", "/api/notifications/**"
    };

    private static final String[] HEALTH = {
        "/api/v1/appointments", "/api/v1/appointments/**",
        "/api/v1/certificate", "/api/v1/certificate/**",
        "/api/v1/episodes", "/api/v1/episodes/**",
        "/api/v1/orders", "/api/v1/orders/**",
        "/api/v1/petcares", "/api/v1/petcares/**",
        "/api/v1/procedures", "/api/v1/procedures/**",
        "/api/v1/spaces", "/api/v1/spaces/**",
        "/api/v1/specialtie", "/api/v1/specialtie/**",
        "/api/v1/specialtiedoctor", "/api/v1/specialtiedoctor/**",
        "/api/v1/vitalsigns", "/api/v1/vitalsigns/**"
    };

    private static final String[] COMMERCIAL = {
        "/api/v1/healthplans", "/api/v1/healthplans/**",
        "/api/v1/providers", "/api/v1/providers/**",
        "/api/v1/purchases", "/api/v1/purchases/**"
    };

    private static final String[] INVENTORY = {
        "/api/headquarter", "/api/headquarter/**",
        "/api/transfers", "/api/transfers/**"
    };

    private static final String[] PAYMENTS = {
        "/api/invoices", "/api/invoices/**",
        "/api/v1/Egresses", "/api/v1/newEgress", "/api/v1/metodosDePago",
        "/api/v1/receipts", "/api/v1/receipts/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Value("${ucopet.cors.allowed-origins:http://localhost:4200}")
                          List<String> allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(CsrfConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                    .requestMatchers(HttpMethod.POST, PERSONS + "/login").permitAll()
                    .requestMatchers(HttpMethod.GET, PERSONS + "/me").authenticated()
                    .requestMatchers(HttpMethod.POST, PERSONS + "/logout").authenticated()
                    .requestMatchers(HttpMethod.POST, PERSONS).hasRole(Role.ADMIN.name())
                    .requestMatchers(HttpMethod.DELETE, PERSONS_ALL).hasRole(Role.ADMIN.name())
                    .requestMatchers(HttpMethod.GET, PERSONS, PERSONS_ALL)
                            .hasAnyRole(Role.ADMIN.name(), Role.DOCTOR.name())
                    .requestMatchers(PERSONS, PERSONS_ALL).authenticated()

                    .requestMatchers(NOTIFICATIONS).authenticated()
                    .requestMatchers(HttpMethod.GET, USERS)
                            .hasAnyRole(Role.ADMIN.name(), Role.DOCTOR.name())
                    .requestMatchers(USERS).hasRole(Role.ADMIN.name())

                    .requestMatchers(HEALTH)
                            .hasAnyRole(Role.ADMIN.name(), Role.DOCTOR.name())

                    .requestMatchers(COMMERCIAL).hasRole(Role.ADMIN.name())
                    .requestMatchers(INVENTORY).hasRole(Role.ADMIN.name())
                    .requestMatchers(PAYMENTS).hasRole(Role.ADMIN.name())

                    .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint(
                    (_, res, _) -> res.sendError(HttpStatus.UNAUTHORIZED.value())))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return _ -> {
            throw new AuthenticationServiceException("La autenticacion la hace el filtro JWT");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
