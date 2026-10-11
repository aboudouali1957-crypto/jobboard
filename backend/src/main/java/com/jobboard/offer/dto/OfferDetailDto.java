package com.jobboard.offer.dto;

import com.jobboard.company.CompanyDto;
import com.jobboard.offer.ContractType;
import com.jobboard.offer.Offer;
import com.jobboard.offer.OfferStatus;
import com.jobboard.skill.SkillDto;

import java.time.Instant;
import java.util.List;

public record OfferDetailDto(
        Long id,
        String title,
        String description,
        String city,
        ContractType contractType,
        Integer salaryMin,
        Integer salaryMax,
        boolean remote,
        OfferStatus status,
        CompanyDto company,
        List<SkillDto> skills,
        Instant createdAt,
        Instant updatedAt) {

    public static OfferDetailDto from(Offer offer) {
        return new OfferDetailDto(
                offer.getId(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getCity(),
                offer.getContractType(),
                offer.getSalaryMin(),
                offer.getSalaryMax(),
                offer.isRemote(),
                offer.getStatus(),
                CompanyDto.from(offer.getCompany()),
                SkillDto.sorted(offer.getSkills()),
                offer.getCreatedAt(),
                offer.getUpdatedAt());
    }
}
