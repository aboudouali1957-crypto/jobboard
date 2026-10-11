package com.jobboard.company;

import com.jobboard.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Entreprises")
public class CompanyController {

    private final CompanyService companyService;
    private final CurrentUser currentUser;

    public CompanyController(CompanyService companyService, CurrentUser currentUser) {
        this.companyService = companyService;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/companies/{id}")
    @Operation(summary = "Consulter la fiche publique d'une entreprise")
    public CompanyDto findById(@PathVariable Long id) {
        return companyService.findById(id);
    }

    @GetMapping("/api/recruiter/company")
    @Operation(summary = "Consulter l'entreprise du recruteur connecté")
    public CompanyDto myCompany() {
        return companyService.findForRecruiter(currentUser.id());
    }

    @PutMapping("/api/recruiter/company")
    @Operation(summary = "Mettre à jour l'entreprise du recruteur connecté")
    public CompanyDto updateMyCompany(@Valid @RequestBody CompanyRequest request) {
        return companyService.updateForRecruiter(currentUser.id(), request);
    }
}
