package com.jobboard;

import com.jobboard.company.Company;
import com.jobboard.config.AppProperties;
import com.jobboard.offer.ContractType;
import com.jobboard.offer.Offer;
import com.jobboard.user.Role;
import com.jobboard.user.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

/**
 * Fabrique d'objets pour les tests unitaires (sans contexte Spring ni base de données).
 */
public final class TestData {

    public static final String JWT_SECRET = "test-secret-for-unit-tests-0123456789abcdef";

    private TestData() {
    }

    public static AppProperties properties() {
        return new AppProperties(
                "http://localhost:4200",
                new AppProperties.Jwt(JWT_SECRET, Duration.ofHours(1), "jobboard-api"),
                new AppProperties.Cors(List.of("http://localhost:4200")),
                new AppProperties.Storage("target/test-storage"),
                new AppProperties.Mail("JobBoard <no-reply@jobboard.dev>", true),
                new AppProperties.Admin("admin@jobboard.dev", "Admin123!"),
                new AppProperties.Seed(false));
    }

    public static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    public static User candidate(Long id, boolean withCv) {
        User user = withId(new User("camille@example.com", "hash", Role.CANDIDATE, "Camille", "Martin"), id);
        if (withCv) {
            user.attachCv("stored.pdf", "cv-camille.pdf");
        }
        return user;
    }

    public static User recruiter(Long id, Company company) {
        User user = withId(new User("sophie@example.com", "hash", Role.RECRUITER, "Sophie", "Bernard"), id);
        user.setCompany(company);
        return user;
    }

    public static Company company(Long id) {
        return withId(new Company("Nimbus Tech", "Paris"), id);
    }

    public static Offer offer(Long id, User recruiter) {
        Offer offer = withId(new Offer(recruiter, recruiter.getCompany()), id);
        offer.setTitle("Développeur Full Stack");
        offer.setDescription("Une description suffisamment longue pour l'offre de test.");
        offer.setCity("Paris");
        offer.setContractType(ContractType.CDI);
        return offer;
    }
}
