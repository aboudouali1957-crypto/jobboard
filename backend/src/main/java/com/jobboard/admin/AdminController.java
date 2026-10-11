package com.jobboard.admin;

import com.jobboard.admin.dto.AdminStatsDto;
import com.jobboard.admin.dto.AdminUserDto;
import com.jobboard.admin.dto.SkillRequest;
import com.jobboard.admin.dto.UserEnabledRequest;
import com.jobboard.common.PageResponse;
import com.jobboard.offer.OfferService;
import com.jobboard.security.CurrentUser;
import com.jobboard.skill.SkillDto;
import com.jobboard.skill.SkillService;
import com.jobboard.user.Role;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration")
public class AdminController {

    private final AdminStatsService statsService;
    private final AdminUserService userService;
    private final OfferService offerService;
    private final SkillService skillService;
    private final CurrentUser currentUser;

    public AdminController(AdminStatsService statsService, AdminUserService userService, OfferService offerService,
                           SkillService skillService, CurrentUser currentUser) {
        this.statsService = statsService;
        this.userService = userService;
        this.offerService = offerService;
        this.skillService = skillService;
        this.currentUser = currentUser;
    }

    @GetMapping("/stats")
    @Operation(summary = "Indicateurs du tableau de bord")
    public AdminStatsDto stats() {
        return statsService.compute();
    }

    @GetMapping("/users")
    @Operation(summary = "Rechercher des utilisateurs par rôle, nom ou email")
    public PageResponse<AdminUserDto> users(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.search(role, search, pageable);
    }

    @PatchMapping("/users/{id}/enabled")
    @Operation(summary = "Activer ou désactiver un compte")
    public AdminUserDto setEnabled(@PathVariable Long id, @Valid @RequestBody UserEnabledRequest request) {
        return userService.setEnabled(currentUser.id(), id, request.enabled());
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une compétence au référentiel")
    public SkillDto createSkill(@Valid @RequestBody SkillRequest request) {
        return skillService.create(request.name());
    }

    @DeleteMapping("/offers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une offre (modération)")
    public void deleteOffer(@PathVariable Long id) {
        offerService.deleteAsAdmin(id);
    }
}
