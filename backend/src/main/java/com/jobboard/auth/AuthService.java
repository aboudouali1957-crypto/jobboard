package com.jobboard.auth;

import com.jobboard.auth.dto.AuthResponse;
import com.jobboard.auth.dto.LoginRequest;
import com.jobboard.auth.dto.RegisterCandidateRequest;
import com.jobboard.auth.dto.RegisterRecruiterRequest;
import com.jobboard.common.exception.ConflictException;
import com.jobboard.common.exception.NotFoundException;
import com.jobboard.company.Company;
import com.jobboard.company.CompanyRepository;
import com.jobboard.security.JwtService;
import com.jobboard.user.Role;
import com.jobboard.user.User;
import com.jobboard.user.UserDto;
import com.jobboard.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthService {

    private final UserRepository users;
    private final CompanyRepository companies;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, CompanyRepository companies,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.companies = companies;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse registerCandidate(RegisterCandidateRequest request) {
        String email = normalize(request.email());
        ensureEmailAvailable(email);
        User user = new User(email, passwordEncoder.encode(request.password()), Role.CANDIDATE,
                request.firstName().trim(), request.lastName().trim());
        return authenticated(users.save(user));
    }

    public AuthResponse registerRecruiter(RegisterRecruiterRequest request) {
        String email = normalize(request.email());
        ensureEmailAvailable(email);
        Company company = companies.save(new Company(request.companyName().trim(), request.companyCity()));
        User user = new User(email, passwordEncoder.encode(request.password()), Role.RECRUITER,
                request.firstName().trim(), request.lastName().trim());
        user.setCompany(company);
        return authenticated(users.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(normalize(request.email()))
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));
        if (!user.isEnabled()) {
            throw new DisabledException("Ce compte a été désactivé par un administrateur");
        }
        return authenticated(user);
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return users.findById(userId)
                .map(UserDto::from)
                .orElseThrow(() -> NotFoundException.of("Utilisateur", userId));
    }

    private AuthResponse authenticated(User user) {
        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResponse(token.value(), token.expiresAt(), UserDto.from(user));
    }

    private void ensureEmailAvailable(String email) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Un compte existe déjà avec cet email");
        }
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
