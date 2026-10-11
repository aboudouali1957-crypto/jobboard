package com.jobboard.auth;

import com.jobboard.auth.dto.AuthResponse;
import com.jobboard.auth.dto.LoginRequest;
import com.jobboard.auth.dto.RegisterCandidateRequest;
import com.jobboard.auth.dto.RegisterRecruiterRequest;
import com.jobboard.security.CurrentUser;
import com.jobboard.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/register/candidate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un compte candidat")
    public AuthResponse registerCandidate(@Valid @RequestBody RegisterCandidateRequest request) {
        return authService.registerCandidate(request);
    }

    @PostMapping("/register/recruiter")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un compte recruteur et son entreprise")
    public AuthResponse registerRecruiter(@Valid @RequestBody RegisterRecruiterRequest request) {
        return authService.registerRecruiter(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Se connecter et obtenir un token JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Utilisateur connecté")
    public UserDto me() {
        return authService.me(currentUser.id());
    }
}
