package com.uco.ucopetapi.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class UnknownEmailAttempts {

    private static final int MAX_EMAILS = 10_000;

    private final Map<String, Attempt> byEmail = new ConcurrentHashMap<>();

    public Instant lockedUntil(String email) {
        Attempt attempt = byEmail.get(normalize(email));
        if (attempt == null || attempt.until() == null || !attempt.until().isAfter(Instant.now())) {
            return null;
        }
        return attempt.until();
    }

    public int addFailure(String email) {
        String key = normalize(email);
        if (byEmail.size() >= MAX_EMAILS && !byEmail.containsKey(key)) {
            purgeExpired();
            if (byEmail.size() >= MAX_EMAILS) {
                return 0;
            }
        }
        return byEmail.compute(key,
                (_, previous) -> new Attempt(previous == null ? 1 : previous.failures() + 1,
                        previous == null ? null : previous.until())).failures();
    }

    public void lock(String email, Instant until) {
        byEmail.computeIfPresent(normalize(email),
                (_, previous) -> new Attempt(previous.failures(), until));
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        byEmail.values().removeIf(r -> r.until() != null && !r.until().isAfter(now));
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private record Attempt(int failures, Instant until) {
    }
}
