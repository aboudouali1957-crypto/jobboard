package com.jobboard.application.dto;

import com.jobboard.application.ApplicationStatus;
import com.jobboard.application.JobApplication;
import com.jobboard.offer.ContractType;
import com.jobboard.offer.Offer;
import com.jobboard.offer.OfferStatus;

import java.time.Instant;

/**
 * Candidature vue par le candidat.
 */
public record CandidateApplicationDto(
        Long id,
        Long offerId,
        String offerTitle,
        String companyName,
        String offerCity,
        ContractType contractType,
        OfferStatus offerStatus,
        ApplicationStatus status,
        String recruiterMessage,
        Instant createdAt,
        Instant updatedAt) {

    public static CandidateApplicationDto from(JobApplication application) {
        Offer offer = application.getOffer();
        return new CandidateApplicationDto(
                application.getId(),
                offer.getId(),
                offer.getTitle(),
                offer.getCompany().getName(),
                offer.getCity(),
                offer.getContractType(),
                offer.getStatus(),
                application.getStatus(),
                application.getRecruiterMessage(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }
}
