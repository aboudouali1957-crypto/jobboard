package com.jobboard.offer;

import com.jobboard.skill.Skill;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Construit dynamiquement la requête de recherche d'offres (API Criteria) à partir des filtres saisis.
 */
public final class OfferSpecifications {

    private OfferSpecifications() {
    }

    public static Specification<Offer> search(OfferSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), OfferStatus.OPEN));

            if (StringUtils.hasText(criteria.keyword())) {
                String pattern = "%" + escapeLike(criteria.keyword().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("title")), pattern, ESCAPE),
                        cb.like(cb.lower(root.<String>get("description")), pattern, ESCAPE),
                        cb.like(cb.lower(root.get("company").<String>get("name")), pattern, ESCAPE)));
            }

            if (StringUtils.hasText(criteria.city())) {
                predicates.add(cb.equal(cb.lower(root.<String>get("city")),
                        criteria.city().trim().toLowerCase(Locale.ROOT)));
            }

            if (criteria.contractType() != null) {
                predicates.add(cb.equal(root.get("contractType"), criteria.contractType()));
            }

            if (criteria.minSalary() != null) {
                // Une offre correspond si le haut de sa fourchette (ou à défaut le bas) atteint le minimum demandé
                Expression<Integer> bestSalary = cb.coalesce(root.<Integer>get("salaryMax"), root.<Integer>get("salaryMin"));
                predicates.add(cb.greaterThanOrEqualTo(bestSalary, criteria.minSalary()));
            }

            if (criteria.skillId() != null) {
                Join<Offer, Skill> skills = root.join("skills");
                predicates.add(cb.equal(skills.get("id"), criteria.skillId()));
            }

            if (Boolean.TRUE.equals(criteria.remote())) {
                predicates.add(cb.isTrue(root.<Boolean>get("remote")));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /** Caractère d'échappement neutre (le backslash est lui-même interprété par MySQL). */
    private static final char ESCAPE = '!';

    private static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
