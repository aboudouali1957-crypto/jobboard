package com.jobboard.offer;

import com.jobboard.common.PageResponse;
import com.jobboard.offer.dto.OfferDetailDto;
import com.jobboard.offer.dto.OfferSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@Tag(name = "Offres (public)", description = "Recherche et consultation des offres ouvertes, sans authentification")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    @Operation(summary = "Rechercher des offres ouvertes",
            description = "Tous les filtres sont optionnels. Tri par défaut : les plus récentes d'abord.")
    public PageResponse<OfferSummaryDto> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) Boolean remote,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OfferSearchCriteria criteria = new OfferSearchCriteria(keyword, city, contractType, minSalary, skillId, remote);
        return offerService.search(criteria, pageable);
    }

    @GetMapping("/cities")
    @Operation(summary = "Villes ayant au moins une offre ouverte (pour les filtres)")
    public List<String> cities() {
        return offerService.openCities();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une offre ouverte")
    public OfferDetailDto findById(@PathVariable Long id) {
        return offerService.findPublished(id);
    }
}
