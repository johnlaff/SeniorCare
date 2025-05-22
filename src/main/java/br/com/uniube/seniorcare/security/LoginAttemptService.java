package br.com.uniube.seniorcare.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPT = 5;
    private static final long BLOCK_TIME_MILLIS = 15 * 60 * 1000; // 15 minutos

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        int current = attempts.getOrDefault(email, 0) + 1;
        attempts.put(email, current);
        if (current >= MAX_ATTEMPT) {
            blockedUntil.put(email, Instant.now().toEpochMilli() + BLOCK_TIME_MILLIS);
        }
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
        blockedUntil.remove(email);
    }

    public boolean isBlocked(String email) {
        Long until = blockedUntil.get(email);
        if (until == null) return false;
        if (Instant.now().toEpochMilli() > until) {
            blockedUntil.remove(email);
            attempts.remove(email);
            return false;
        }
        return true;
    }
}
