package com.uco.ucopetapi.security;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthCookie {
    public static final String NAME = "ucopet_auth";
    private static final String PATH = "/api";

    private final Duration maxAge;
    private final boolean secure;

    public AuthCookie(@Value("${ucopet.jwt.expiration-minutes:720}") long minutes,
                      @Value("${ucopet.cookie.secure:false}") boolean secure) {
        this.maxAge = Duration.ofMinutes(minutes);
        this.secure = secure;
    }

    public String create(String token) {
        return build(token, maxAge);
    }

    public String clear() {
        return build("", Duration.ZERO);
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private String build(String value, Duration duration) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(PATH)
                .maxAge(duration)
                .build()
                .toString();
    }
}
