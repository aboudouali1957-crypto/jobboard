package com.jobboard.offer.dto;

import com.jobboard.application.ApplicationStatus;
import com.jobboard.offer.Offer;

import java.util.EnumMap;
import java.util.Map;

/**
 * Offre vue par son recruteur, avec le nombre de candidatures pour chaque statut.
 */
public record RecruiterOfferDto(
        OfferSummaryDto offer,
        long totalApplications,
        Map<ApplicationStatus, Long> applicationsByStatus) {

    public static RecruiterOfferDto from(Offer offer, Map<ApplicationStatus, Long> counts) {
        Map<ApplicationStatus, Long> byStatus = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status, counts.getOrDefault(status, 0L));
        }
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new RecruiterOfferDto(OfferSummaryDto.from(offer), total, byStatus);
    }
}
