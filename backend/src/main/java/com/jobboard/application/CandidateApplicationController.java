package com.jobboard.application;

import com.jobboard.application.dto.ApplyRequest;
import com.jobboard.application.dto.CandidateApplicationDto;
import com.jobboard.common.PageResponse;
import com.jobboard.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidate/applications")
@Tag(name = "Candidat - candidatures")
public class CandidateApplicationController {

    private final ApplicationService applicationService;
    private final CurrentUser currentUser;

    public CandidateApplicationController(ApplicationService applicationService, CurrentUser currentUser) {
        this.applicationService = applicationService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Postuler à une offre", description = "Nécessite un CV déposé sur le profil.")
    public CandidateApplicationDto apply(@Valid @RequestBody ApplyRequest request) {
        return applicationService.apply(currentUser.id(), request);
    }

    @GetMapping
    @Operation(summary = "Suivre mes candidatures, éventuellement filtrées par statut")
    public PageResponse<CandidateApplicationDto> myApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return applicationService.findForCandidate(currentUser.id(), status, pageable);
    }

    @GetMapping("/offer-ids")
    @Operation(summary = "Identifiants des offres auxquelles j'ai déjà postulé")
    public List<Long> appliedOfferIds() {
        return applicationService.appliedOfferIds(currentUser.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retirer une candidature encore au statut « reçue »")
    public void withdraw(@PathVariable Long id) {
        applicationService.withdraw(currentUser.id(), id);
    }
}
