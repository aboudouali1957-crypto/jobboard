package com.jobboard.application;

import com.jobboard.common.BaseEntity;
import com.jobboard.common.exception.BusinessRuleException;
import com.jobboard.offer.Offer;
import com.jobboard.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Candidature d'un candidat à une offre (table "applications").
 */
@Entity
@Table(name = "applications")
public class JobApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.RECEIVED;

    @Column(name = "cover_letter")
    private String coverLetter;

    /** Dernier message du recruteur, transmis au candidat avec la notification de changement de statut. */
    @Column(name = "recruiter_message", length = 1000)
    private String recruiterMessage;

    protected JobApplication() {
        // requis par JPA
    }

    public JobApplication(Offer offer, User candidate, String coverLetter) {
        this.offer = offer;
        this.candidate = candidate;
        this.coverLetter = coverLetter;
    }

    /**
     * Fait évoluer la candidature en respectant le cycle de vie défini par {@link ApplicationStatus}.
     */
    public void moveTo(ApplicationStatus target, String message) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessRuleException(
                    "Transition impossible de " + status + " vers " + target);
        }
        this.status = target;
        this.recruiterMessage = message;
    }

    public Offer getOffer() {
        return offer;
    }

    public User getCandidate() {
        return candidate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public String getRecruiterMessage() {
        return recruiterMessage;
    }
}
