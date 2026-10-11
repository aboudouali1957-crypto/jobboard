package com.jobboard.offer.dto;

import com.jobboard.offer.ContractType;
import com.jobboard.offer.Offer;
import com.jobboard.offer.OfferStatus;
import com.jobboard.skill.SkillDto;

import java.time.Instant;
import java.util.List;

/**
 * Vue condensée d'une offre, utilisée dans les listes de résultats.
 */
public record OfferSummaryDto(
        Long id,
        String title,
        String city,
        ContractType contractType,
        Integer salaryMin,
        Integer salaryMax,
        boolean remote,
        OfferStatus status,
        Long companyId,
        String companyName,
        List<String> skills,
        Instant createdAt) {

    public static OfferSummaryDto from(Offer offer) {
        return new OfferSummaryDto(
                offer.getId(),
                offer.getTitle(),
                offer.getCity(),
                offer.getContractType(),
                offer.getSalaryMin(),
                offer.getSalaryMax(),
                offer.isRemote(),
                offer.getStatus(),
                offer.getCompany().getId(),
                offer.getCompany().getName(),
                SkillDto.names(offer.getSkills()),
                offer.getCreatedAt());
    }
}
