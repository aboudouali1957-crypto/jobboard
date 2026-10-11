package com.jobboard.application;

import com.jobboard.application.dto.ApplyRequest;
import com.jobboard.application.dto.CandidateApplicationDto;
import com.jobboard.application.dto.ChangeStatusRequest;
import com.jobboard.application.dto.RecruiterApplicationDto;
import com.jobboard.common.PageResponse;
import com.jobboard.common.exception.BusinessRuleException;
import com.jobboard.common.exception.ConflictException;
import com.jobboard.common.exception.NotFoundException;
import com.jobboard.offer.Offer;
import com.jobboard.offer.OfferRepository;
import com.jobboard.storage.CvFile;
import com.jobboard.storage.FileStorageService;
import com.jobboard.user.User;
import com.jobboard.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class ApplicationService {

    private final JobApplicationRepository applications;
    private final OfferRepository offers;
    private final UserRepository users;
    private final FileStorageService storage;
    private final ApplicationEventPublisher events;

    public ApplicationService(JobApplicationRepository applications, OfferRepository offers, UserRepository users,
                              FileStorageService storage, ApplicationEventPublisher events) {
        this.applications = applications;
        this.offers = offers;
        this.users = users;
        this.storage = storage;
        this.events = events;
    }

    // ------------------------------------------------------------------ Candidat

    public CandidateApplicationDto apply(Long candidateId, ApplyRequest request) {
        User candidate = users.findById(candidateId)
                .orElseThrow(() -> NotFoundException.of("Utilisateur", candidateId));
        Offer offer = offers.findById(request.offerId())
                .orElseThrow(() -> NotFoundException.of("Offre", request.offerId()));

        if (!offer.isOpen()) {
            throw new BusinessRuleException("Cette offre n'accepte plus de candidatures");
        }
        if (!candidate.hasCv()) {
            throw new BusinessRuleException("Ajoutez votre CV à votre profil avant de postuler");
        }
        if (applications.existsByOfferIdAndCandidateId(offer.getId(), candidateId)) {
            throw new ConflictException("Vous avez déjà postulé à cette offre");
        }

        String coverLetter = StringUtils.hasText(request.coverLetter()) ? request.coverLetter().trim() : null;
        JobApplication application = applications.save(new JobApplication(offer, candidate, coverLetter));
        events.publishEvent(ApplicationStatusChangedEvent.of(application));
        return CandidateApplicationDto.from(application);
    }

    @Transactional(readOnly = true)
    public PageResponse<CandidateApplicationDto> findForCandidate(Long candidateId, ApplicationStatus status,
                                                                  Pageable pageable) {
        return PageResponse.of(applications.findForCandidate(candidateId, status, pageable),
                CandidateApplicationDto::from);
    }

    @Transactional(readOnly = true)
    public List<Long> appliedOfferIds(Long candidateId) {
        return applications.findOfferIdsByCandidateId(candidateId);
    }

    public void withdraw(Long candidateId, Long applicationId) {
        JobApplication application = applications.findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> NotFoundException.of("Candidature", applicationId));
        if (application.getStatus() != ApplicationStatus.RECEIVED) {
            throw new BusinessRuleException("Seule une candidature encore au statut « reçue » peut être retirée");
        }
        applications.delete(application);
    }

    // ------------------------------------------------------------------ Recruteur

    @Transactional(readOnly = true)
    public PageResponse<RecruiterApplicationDto> findForRecruiter(Long recruiterId, Long offerId,
                                                                  ApplicationStatus status, Pageable pageable) {
        return PageResponse.of(applications.findForRecruiter(recruiterId, offerId, status, pageable),
                RecruiterApplicationDto::from);
    }

    public RecruiterApplicationDto changeStatus(Long recruiterId, Long applicationId, ChangeStatusRequest request) {
        JobApplication application = recruiterApplication(recruiterId, applicationId);
        String message = StringUtils.hasText(request.message()) ? request.message().trim() : null;
        application.moveTo(request.status(), message);
        applications.saveAndFlush(application);

        // Traité après le commit par ApplicationNotificationListener (envoi de l'email)
        events.publishEvent(ApplicationStatusChangedEvent.of(application));
        return RecruiterApplicationDto.from(application);
    }

    @Transactional(readOnly = true)
    public CvFile candidateCv(Long recruiterId, Long applicationId) {
        User candidate = recruiterApplication(recruiterId, applicationId).getCandidate();
        if (!candidate.hasCv()) {
            throw new NotFoundException("Ce candidat n'a pas déposé de CV");
        }
        return new CvFile(storage.load(candidate.getCvStorageKey()), candidate.getCvOriginalName());
    }

    private JobApplication recruiterApplication(Long recruiterId, Long applicationId) {
        // Une candidature d'un autre recruteur est volontairement traitée comme introuvable
        return applications.findByIdForRecruiter(applicationId, recruiterId)
                .orElseThrow(() -> NotFoundException.of("Candidature", applicationId));
    }
}
