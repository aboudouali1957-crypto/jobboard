package com.jobboard.offer;

import com.jobboard.offer.dto.OfferRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OfferRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void valid_request_has_no_violation() {
        assertThat(validator.validate(request(40000, 50000))).isEmpty();
    }

    @Test
    void salary_range_must_be_consistent() {
        Set<ConstraintViolation<OfferRequest>> violations = validator.validate(request(50000, 40000));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).containsExactly("salaryRangeValid");
    }

    @Test
    void salaries_are_optional() {
        assertThat(validator.validate(request(null, null))).isEmpty();
    }

    @Test
    void title_and_contract_type_are_required() {
        OfferRequest request = new OfferRequest(" ", "Une description suffisamment longue pour être acceptée.",
                "Lyon", null, null, null, false, Set.of());

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("title", "contractType");
    }

    private static OfferRequest request(Integer min, Integer max) {
        return new OfferRequest("Développeur Java", "Une description suffisamment longue pour être acceptée.",
                "Lyon", ContractType.CDI, min, max, true, Set.of(1L, 2L));
    }
}
