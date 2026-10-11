package com.jobboard.company;

import com.jobboard.common.exception.NotFoundException;
import com.jobboard.user.User;
import com.jobboard.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companies;
    private final UserRepository users;

    public CompanyService(CompanyRepository companies, UserRepository users) {
        this.companies = companies;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public CompanyDto findById(Long id) {
        return companies.findById(id)
                .map(CompanyDto::from)
                .orElseThrow(() -> NotFoundException.of("Entreprise", id));
    }

    @Transactional(readOnly = true)
    public CompanyDto findForRecruiter(Long recruiterId) {
        return CompanyDto.from(companyOf(recruiterId));
    }

    public CompanyDto updateForRecruiter(Long recruiterId, CompanyRequest request) {
        Company company = companyOf(recruiterId);
        company.setName(request.name().trim());
        company.setDescription(request.description());
        company.setWebsite(request.website());
        company.setCity(request.city());
        // Entité managée : les modifications sont persistées au commit de la transaction
        return CompanyDto.from(company);
    }

    private Company companyOf(Long recruiterId) {
        User recruiter = users.findById(recruiterId)
                .orElseThrow(() -> NotFoundException.of("Utilisateur", recruiterId));
        if (recruiter.getCompany() == null) {
            throw new NotFoundException("Aucune entreprise n'est associée à ce compte");
        }
        return recruiter.getCompany();
    }
}
