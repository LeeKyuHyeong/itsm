package com.itsm.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
public class JwtBlacklist {

    private final Cache<String, Boolean> blacklist = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(2))
            .maximumSize(100_000)
            .build();

    public void blacklist(String token, Claims claims) {
        long remainingMs = claims.getExpiration().getTime() - new Date().getTime();
        if (remainingMs > 0) {
            blacklist.put(token, true);
        }
    }

    public boolean isBlacklisted(String token) {
        return blacklist.getIfPresent(token) != null;
    }
}
