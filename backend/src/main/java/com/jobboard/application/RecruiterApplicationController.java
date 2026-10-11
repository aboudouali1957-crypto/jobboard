package com.jobboard.application;

import com.jobboard.application.dto.ChangeStatusRequest;
import com.jobboard.application.dto.RecruiterApplicationDto;
import com.jobboard.common.PageResponse;
import com.jobboard.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter/applications")
@Tag(name = "Recruteur - candidatures", description = "Suivi des candidatures reçues sur mes offres")
public class RecruiterApplicationController {

    private final ApplicationService applicationService;
    private final CurrentUser currentUser;

    public RecruiterApplicationController(ApplicationService applicationService, CurrentUser currentUser) {
        this.applicationService = applicationService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Lister les candidatures reçues, filtrables par offre et par statut")
    public PageResponse<RecruiterApplicationDto> received(
            @RequestParam(required = false) Long offerId,
            @RequestParam(required = false) ApplicationStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return applicationService.findForRecruiter(currentUser.id(), offerId, status, pageable);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Faire évoluer le statut d'une candidature",
            description = "RECEIVED → INTERVIEW | REJECTED, puis INTERVIEW → ACCEPTED | REJECTED. Le candidat est notifié par email.")
    public RecruiterApplicationDto changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return applicationService.changeStatus(currentUser.id(), id, request);
    }

    @GetMapping("/{id}/cv")
    @Operation(summary = "Télécharger le CV du candidat")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id) {
        return applicationService.candidateCv(currentUser.id(), id).toResponse();
    }
}
