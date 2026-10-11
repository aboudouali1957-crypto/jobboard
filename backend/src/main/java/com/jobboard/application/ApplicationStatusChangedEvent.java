package com.jobboard.application;

/**
 * Publié à chaque création ou changement de statut d'une candidature.
 * Il embarque toutes les données nécessaires à la notification pour être traité
 * de façon asynchrone, après le commit, sans dépendre de la session JPA.
 */
public record ApplicationStatusChangedEvent(
        Long applicationId,
        String candidateEmail,
        String candidateFirstName,
        String offerTitle,
        String companyName,
        ApplicationStatus status,
        String recruiterMessage) {

    public static ApplicationStatusChangedEvent of(JobApplication application) {
        return new ApplicationStatusChangedEvent(
                application.getId(),
                application.getCandidate().getEmail(),
                application.getCandidate().getFirstName(),
                application.getOffer().getTitle(),
                application.getOffer().getCompany().getName(),
                application.getStatus(),
                application.getRecruiterMessage());
    }
}
