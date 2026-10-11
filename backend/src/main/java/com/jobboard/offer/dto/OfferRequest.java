package com.jobboard.offer.dto;

import com.jobboard.offer.ContractType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Données de création / modification d'une offre.
 * Les salaires sont exprimés en euros bruts annuels.
 */
public record OfferRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(min = 30, max = 10000) String description,
        @NotBlank @Size(max = 100) String city,
        @NotNull ContractType contractType,
        @PositiveOrZero @Max(1_000_000) Integer salaryMin,
        @PositiveOrZero @Max(1_000_000) Integer salaryMax,
        boolean remote,
        @Size(max = 15) Set<Long> skillIds) {

    @AssertTrue(message = "le salaire maximum doit être supérieur ou égal au salaire minimum")
    public boolean isSalaryRangeValid() {
        return salaryMin == null || salaryMax == null || salaryMax >= salaryMin;
    }
}
