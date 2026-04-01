package com.itsm.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Cache<String, AtomicInteger> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW)
            .maximumSize(10_000)
            .build();

    public boolean isBlocked(String ip) {
        AtomicInteger attempts = attemptCache.getIfPresent(ip);
        return attempts != null && attempts.get() >= MAX_ATTEMPTS;
    }

    public void recordAttempt(String ip) {
        attemptCache.asMap()
                .computeIfAbsent(ip, k -> new AtomicInteger(0))
                .incrementAndGet();
    }
}
