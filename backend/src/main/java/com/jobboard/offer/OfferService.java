package com.jobboard.offer;

import com.jobboard.application.ApplicationStatus;
import com.jobboard.application.JobApplicationRepository;
import com.jobboard.common.PageResponse;
import com.jobboard.common.exception.BusinessRuleException;
import com.jobboard.common.exception.NotFoundException;
import com.jobboard.offer.dto.OfferDetailDto;
import com.jobboard.offer.dto.OfferRequest;
import com.jobboard.offer.dto.OfferSummaryDto;
import com.jobboard.offer.dto.RecruiterOfferDto;
import com.jobboard.skill.SkillService;
import com.jobboard.user.User;
import com.jobboard.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offers;
    private final UserRepository users;
    private final JobApplicationRepository applications;
    private final SkillService skillService;

    public OfferService(OfferRepository offers, UserRepository users,
                        JobApplicationRepository applications, SkillService skillService) {
        this.offers = offers;
        this.users = users;
        this.applications = applications;
        this.skillService = skillService;
    }

    // ------------------------------------------------------------------ Lecture publique

    @Transactional(readOnly = true)
    public PageResponse<OfferSummaryDto> search(OfferSearchCriteria criteria, Pageable pageable) {
        Page<Offer> page = offers.findAll(OfferSpecifications.search(criteria), pageable);
        return PageResponse.of(page, OfferSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public OfferDetailDto findPublished(Long id) {
        return offers.findById(id)
                .filter(Offer::isOpen)
                .map(OfferDetailDto::from)
                .orElseThrow(() -> NotFoundException.of("Offre", id));
    }

    @Transactional(readOnly = true)
    public List<String> openCities() {
        return offers.findDistinctCities(OfferStatus.OPEN);
    }

    // ------------------------------------------------------------------ Espace recruteur

    @Transactional(readOnly = true)
    public PageResponse<RecruiterOfferDto> findByRecruiter(Long recruiterId, Pageable pageable) {
        Page<Offer> page = offers.findByRecruiterId(recruiterId, pageable);
        List<Long> offerIds = page.getContent().stream().map(Offer::getId).toList();

        Map<Long, Map<ApplicationStatus, Long>> counts = new HashMap<>();
        if (!offerIds.isEmpty()) {
            for (Object[] row : applications.countGroupedByOfferAndStatus(offerIds)) {
                Long offerId = (Long) row[0];
                ApplicationStatus status = (ApplicationStatus) row[1];
                long count = ((Number) row[2]).longValue();
                counts.computeIfAbsent(offerId, id -> new EnumMap<ApplicationStatus, Long>(ApplicationStatus.class))
                        .put(status, count);
            }
        }
        return PageResponse.of(page, offer -> RecruiterOfferDto.from(offer, counts.getOrDefault(offer.getId(), Map.of())));
    }

    @Transactional(readOnly = true)
    public OfferDetailDto findOwned(Long recruiterId, Long offerId) {
        return OfferDetailDto.from(ownedOffer(recruiterId, offerId));
    }

    public OfferDetailDto create(Long recruiterId, OfferRequest request) {
        User recruiter = users.findById(recruiterId)
                .orElseThrow(() -> NotFoundException.of("Utilisateur", recruiterId));
        if (recruiter.getCompany() == null) {
            throw new BusinessRuleException("Aucune entreprise n'est associée à ce compte recruteur");
        }
        Offer offer = new Offer(recruiter, recruiter.getCompany());
        applyRequest(offer, request);
        return OfferDetailDto.from(offers.save(offer));
    }

    public OfferDetailDto update(Long recruiterId, Long offerId, OfferRequest request) {
        Offer offer = ownedOffer(recruiterId, offerId);
        applyRequest(offer, request);
        return OfferDetailDto.from(offers.saveAndFlush(offer));
    }

    public OfferDetailDto changeStatus(Long recruiterId, Long offerId, OfferStatus status) {
        Offer offer = ownedOffer(recruiterId, offerId);
        offer.setStatus(status);
        return OfferDetailDto.from(offers.saveAndFlush(offer));
    }

    public void delete(Long recruiterId, Long offerId) {
        deleteOffer(ownedOffer(recruiterId, offerId));
    }

    // ------------------------------------------------------------------ Modération (admin)

    public void deleteAsAdmin(Long offerId) {
        deleteOffer(offers.findById(offerId).orElseThrow(() -> NotFoundException.of("Offre", offerId)));
    }

    // ------------------------------------------------------------------ Interne

    private void deleteOffer(Offer offer) {
        applications.deleteByOfferId(offer.getId());
        offers.delete(offer);
    }

    private Offer ownedOffer(Long recruiterId, Long offerId) {
        Offer offer = offers.findById(offerId).orElseThrow(() -> NotFoundException.of("Offre", offerId));
        if (!offer.isOwnedBy(recruiterId)) {
            throw new AccessDeniedException("Cette offre appartient à un autre recruteur");
        }
        return offer;
    }

    private void applyRequest(Offer offer, OfferRequest request) {
        offer.setTitle(request.title().trim());
        offer.setDescription(request.description().trim());
        offer.setCity(request.city().trim());
        offer.setContractType(request.contractType());
        offer.setSalaryMin(request.salaryMin());
        offer.setSalaryMax(request.salaryMax());
        offer.setRemote(request.remote());
        offer.replaceSkills(skillService.resolve(request.skillIds()));
    }
}
