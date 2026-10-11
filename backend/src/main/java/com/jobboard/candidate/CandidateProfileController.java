package com.jobboard.candidate;

import com.jobboard.candidate.dto.CandidateProfileDto;
import com.jobboard.candidate.dto.UpdateProfileRequest;
import com.jobboard.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidate")
@Tag(name = "Candidat - profil")
public class CandidateProfileController {

    private final CandidateProfileService profileService;
    private final CurrentUser currentUser;

    public CandidateProfileController(CandidateProfileService profileService, CurrentUser currentUser) {
        this.profileService = profileService;
        this.currentUser = currentUser;
    }

    @GetMapping("/profile")
    @Operation(summary = "Consulter mon profil")
    public CandidateProfileDto profile() {
        return profileService.getProfile(currentUser.id());
    }

    @PutMapping("/profile")
    @Operation(summary = "Mettre à jour mon profil et mes compétences")
    public CandidateProfileDto updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(currentUser.id(), request);
    }

    @PostMapping(value = "/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Déposer ou remplacer mon CV (PDF, 5 Mo maximum)")
    public CandidateProfileDto uploadCv(@RequestPart("file") MultipartFile file) {
        return profileService.uploadCv(currentUser.id(), file);
    }

    @GetMapping("/cv")
    @Operation(summary = "Télécharger mon CV")
    public ResponseEntity<Resource> downloadCv() {
        return profileService.loadCv(currentUser.id()).toResponse();
    }
}
