package com.jobboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuration propre à l'application (préfixe {@code app.*} dans application.yml).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendUrl,
        Jwt jwt,
        Cors cors,
        Storage storage,
        Mail mail,
        Admin admin,
        Seed seed) {

    public record Jwt(String secret, Duration expiration, String issuer) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Storage(String cvDir) {
    }

    public record Mail(String from, boolean enabled) {
    }

    public record Admin(String email, String password) {
    }

    public record Seed(boolean demoData) {
    }
}
