package com.jobboard.security;

import com.jobboard.TestData;
import com.jobboard.config.AppProperties;
import com.jobboard.user.Role;
import com.jobboard.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie l'aller-retour complet : émission du token puis validation par le décodeur du resource server.
 */
class JwtServiceTest {

    private final SecurityConfig config = new SecurityConfig();
    private AppProperties properties;
    private JwtService jwtService;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        properties = TestData.properties();
        SecretKey key = config.jwtSecretKey(properties);
        jwtService = new JwtService(config.jwtEncoder(key), properties);
        decoder = config.jwtDecoder(key, properties);
    }

    @Test
    void issued_token_is_accepted_by_the_decoder_and_carries_the_role() {
        User recruiter = TestData.recruiter(7L, TestData.company(1L));

        JwtService.IssuedToken token = jwtService.issue(recruiter);
        Jwt jwt = decoder.decode(token.value());

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString(JwtService.ROLE_CLAIM)).isEqualTo(Role.RECRUITER.name());
        assertThat(jwt.getClaimAsString("email")).isEqualTo("sophie@example.com");
        assertThat(jwt.getExpiresAt()).isEqualTo(token.expiresAt());
    }

    @Test
    void role_claim_is_converted_to_a_spring_authority() {
        Jwt jwt = decoder.decode(jwtService.issue(TestData.candidate(3L, false)).value());

        List<String> authorities = config.jwtAuthenticationConverter().convert(jwt).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).contains("ROLE_CANDIDATE");
    }

    @Test
    void token_signed_with_another_secret_is_rejected() {
        AppProperties other = new AppProperties(properties.frontendUrl(),
                new AppProperties.Jwt("another-secret-that-is-long-enough-0123456789", Duration.ofHours(1), "jobboard-api"),
                properties.cors(), properties.storage(), properties.mail(), properties.admin(), properties.seed());
        SecretKey otherKey = config.jwtSecretKey(other);
        String forged = new JwtService(config.jwtEncoder(otherKey), other).issue(TestData.candidate(3L, false)).value();

        assertThatThrownBy(() -> decoder.decode(forged)).isInstanceOf(JwtException.class);
    }

    @Test
    void too_short_secret_is_refused_at_startup() {
        AppProperties weak = new AppProperties(properties.frontendUrl(),
                new AppProperties.Jwt("short", Duration.ofHours(1), "jobboard-api"),
                properties.cors(), properties.storage(), properties.mail(), properties.admin(), properties.seed());

        assertThatThrownBy(() -> config.jwtSecretKey(weak)).isInstanceOf(IllegalStateException.class);
    }
}
