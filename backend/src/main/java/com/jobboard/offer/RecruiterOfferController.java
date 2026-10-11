package com.jobboard.offer;

import com.jobboard.common.PageResponse;
import com.jobboard.offer.dto.OfferDetailDto;
import com.jobboard.offer.dto.OfferRequest;
import com.jobboard.offer.dto.OfferStatusRequest;
import com.jobboard.offer.dto.RecruiterOfferDto;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter/offers")
@Tag(name = "Recruteur - offres", description = "Gestion des offres du recruteur connecté")
public class RecruiterOfferController {

    private final OfferService offerService;
    private final CurrentUser currentUser;

    public RecruiterOfferController(OfferService offerService, CurrentUser currentUser) {
        this.offerService = offerService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "Lister mes offres avec le décompte des candidatures par statut")
    public PageResponse<RecruiterOfferDto> myOffers(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return offerService.findByRecruiter(currentUser.id(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une de mes offres (y compris fermée)")
    public OfferDetailDto findById(@PathVariable Long id) {
        return offerService.findOwned(currentUser.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publier une offre")
    public OfferDetailDto create(@Valid @RequestBody OfferRequest request) {
        return offerService.create(currentUser.id(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une de mes offres")
    public OfferDetailDto update(@PathVariable Long id, @Valid @RequestBody OfferRequest request) {
        return offerService.update(currentUser.id(), id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ouvrir ou fermer une de mes offres")
    public OfferDetailDto changeStatus(@PathVariable Long id, @Valid @RequestBody OfferStatusRequest request) {
        return offerService.changeStatus(currentUser.id(), id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une de mes offres et ses candidatures")
    public void delete(@PathVariable Long id) {
        offerService.delete(currentUser.id(), id);
    }
}
